package com.example.smartfactoryservice.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.AttendanceRecord;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Department;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.LeaveRecord;
import com.example.smartfactoryservice.Domain.Vo.Person.PersonnelVO;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.AttendanceRecordMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.DepartmentMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.EmployeeMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.LeaveRecordMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * 大屏人事数据推送任务。
 * 仅在有客户端订阅 /topic/personnel 时才执行查询和推送，
 * 最后一个订阅者离开后自动停止。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonnelDataPusher {

    private static final String TOPIC = "/topic/personnel";
    private static final long INTERVAL_MS = 3000;

    private final SimpMessagingTemplate messagingTemplate;
    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final SubscriptionTracker subscriptionTracker;
    private final TaskScheduler taskScheduler;

    private ScheduledFuture<?> scheduledFuture;
    private final Object lock = new Object();

    @PostConstruct
    public void init() {
        subscriptionTracker.register(TOPIC, this::startPushing, this::stopPushing);
    }

    private void startPushing() {
        synchronized (lock) {
            if (scheduledFuture == null || scheduledFuture.isCancelled()) {
                // 立即推送首帧数据，消除首次 3s 延迟
                pushPersonnelData();
                scheduledFuture = taskScheduler.scheduleAtFixedRate(
                        this::pushPersonnelData,
                        new Date(System.currentTimeMillis() + INTERVAL_MS),
                        INTERVAL_MS);
                log.info("人事数据推送已启动: topic={}, interval={}ms", TOPIC, INTERVAL_MS);
            }
        }
    }

    private void stopPushing() {
        synchronized (lock) {
            if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
                scheduledFuture.cancel(false);
                scheduledFuture = null;
                log.info("人事数据推送已停止: topic={}", TOPIC);
            }
        }
    }

    private void pushPersonnelData() {
        try {
            PersonnelVO data = buildPersonnelData();
            messagingTemplate.convertAndSend(TOPIC, data);
            log.debug("人事数据推送成功: working={}, leave={}, offWork={}, total={}",
                    data.getAttendance().getWorking(),
                    data.getAttendance().getLeave(),
                    data.getAttendance().getOffWork(),
                    data.getAttendance().getTotal());
        } catch (Exception e) {
            log.error("推送人事数据失败", e);
        }
    }

    /**
     * 构建人事数据VO
     */
    private PersonnelVO buildPersonnelData() {
        LocalDate today = LocalDate.now();

        // 查询所有未删除的员工
        List<Employee> allEmployees = employeeMapper.selectList(
                new QueryWrapper<Employee>().eq("delete_flag", false));

        // 查询当天的考勤记录
        List<AttendanceRecord> todayRecords = attendanceRecordMapper.selectList(
                new QueryWrapper<AttendanceRecord>().eq("date", today));

        // 查询当天有效的请假记录
        List<LeaveRecord> todayLeaves = leaveRecordMapper.selectList(
                new QueryWrapper<LeaveRecord>()
                        .eq("status", 1)
                        .le("start_date", today)
                        .ge("end_date", today));

        // 当天已上班打卡但未下班的员工ID
        Set<Integer> checkedInEmployeeIds = todayRecords.stream()
                .filter(r -> r.getCheckInTime() != null && r.getCheckOutTime() == null)
                .map(AttendanceRecord::getEmployeeId)
                .collect(Collectors.toSet());

        // 当天已下班打卡的员工ID
        Set<Integer> checkedOutEmployeeIds = todayRecords.stream()
                .filter(r -> r.getCheckOutTime() != null)
                .map(AttendanceRecord::getEmployeeId)
                .collect(Collectors.toSet());

        // 当天请假员工ID
        Set<Integer> leaveEmployeeIds = todayLeaves.stream()
                .map(LeaveRecord::getEmployeeId)
                .collect(Collectors.toSet());

        int total = allEmployees.size();

        // 统计总体数据
        int working = 0;
        int leave = 0;
        int offWork = 0;

        for (Employee emp : allEmployees) {
            Integer empId = emp.getEmployeeId();
            if (leaveEmployeeIds.contains(empId)) {
                leave++;
            } else if (checkedInEmployeeIds.contains(empId)) {
                working++;
            } else if (checkedOutEmployeeIds.contains(empId)) {
                offWork++;
            }
            // 当天未打卡的员工不计入任何分类
        }

        int rate = total > 0 ? (int) Math.round((double) working / total * 100) : 0;

        PersonnelVO.AttendanceSummary summary =
                new PersonnelVO.AttendanceSummary(working, leave, offWork, total, rate);

        // 按部门统计
        Map<Integer, List<Employee>> deptEmployeeMap = allEmployees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartmentId));

        List<Department> departments = departmentMapper.selectList(
                new QueryWrapper<Department>().eq("delete_flag", false));

        List<PersonnelVO.DepartmentAttendance> deptList = new ArrayList<>();
        for (Department dept : departments) {
            List<Employee> deptEmployees = deptEmployeeMap.getOrDefault(dept.getDepartmentId(), List.of());
            int deptTotal = deptEmployees.size();
            if (deptTotal == 0) {
                continue;
            }

            int deptWorking = 0;
            for (Employee emp : deptEmployees) {
                Integer empId = emp.getEmployeeId();
                if (leaveEmployeeIds.contains(empId)) {
                    continue;
                }
                if (checkedInEmployeeIds.contains(empId)) {
                    deptWorking++;
                }
            }

            int deptRate = deptTotal > 0
                    ? (int) Math.round((double) deptWorking / deptTotal * 100)
                    : 0;

            deptList.add(new PersonnelVO.DepartmentAttendance(
                    dept.getDepartmentName(), deptWorking, deptTotal, deptRate));
        }

        return new PersonnelVO(summary, deptList);
    }
}
