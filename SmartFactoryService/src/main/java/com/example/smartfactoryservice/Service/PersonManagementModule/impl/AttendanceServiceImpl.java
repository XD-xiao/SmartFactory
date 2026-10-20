package com.example.smartfactoryservice.Service.PersonManagementModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.AttendanceRecord;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.LeaveRecord;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.AttendanceRecordMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.EmployeeMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.LeaveRecordMapper;
import com.example.smartfactoryservice.Service.PersonManagementModule.AttendanceService;
import com.example.smartfactoryservice.Utils.CheckInOutTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl extends PersonManagementServiceSupport implements AttendanceService {

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final LeaveRecordMapper leaveRecordMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCheckRecord(Map<String, Object> attendance) {
        requireBody(attendance, "考勤信息不能为空");

        Employee employee = resolveEmployeeByNumber(attendance);
        if (employee.isDeleteFlag()) {
            throw new BusinessException(BAD_REQUEST_CODE, "员工已被删除，无法打卡");
        }
        Integer employeeId = employee.getEmployeeId();

        
        

        LocalDateTime checkInTime = LocalDateTime.now();
        LocalDate checkInDate = checkInTime.toLocalDate();
        LocalTime checkInTimeLocal = checkInTime.toLocalTime();

        // 检查员工是否在请假中,查询请假记录表
        QueryWrapper<LeaveRecord> leaveWrapper = new QueryWrapper<>();
        leaveWrapper.eq("employee_id", employeeId)
                    .eq("status", 1)      
                    .le("start_date", checkInDate)  // start_date <= 今天
                    .ge("end_date", checkInDate);   // end_date >= 今天
        List<LeaveRecord> leaveRecords = leaveRecordMapper.selectList(leaveWrapper);
        if (!leaveRecords.isEmpty()) {
            throw new BusinessException(BAD_REQUEST_CODE, "员工已在请假中，不能打卡");
        }


        QueryWrapper<AttendanceRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("employee_id", employeeId)
                .eq("date", checkInTime.toLocalDate())
                .orderByDesc("attendance_id");
        List<AttendanceRecord> todayRecords = attendanceRecordMapper.selectList(wrapper);
        if (!todayRecords.isEmpty()) {
            throw new BusinessException(BAD_REQUEST_CODE, "当天已存在考勤记录，请勿重复打卡");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId(employeeId);
        record.setDate(checkInTime.toLocalDate());
        record.setCheckInTime(checkInTime);

        record.setStatus(checkInTimeLocal.isBefore(CheckInOutTime.checkInTime) ? 1 : 2);

        record.setWorkHours(BigDecimal.ZERO);
        attendanceRecordMapper.insert(record);

        // 修改员工状态为正常
        employee.setStatus(1);
        employeeMapper.updateById(employee);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCheckRecord(Integer attendanceID) {
        if (attendanceID == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "考勤记录ID不能为空");
        }

        AttendanceRecord record = attendanceRecordMapper.selectById(attendanceID);
        if (record == null) {
            throw new BusinessException(NOT_FOUND_CODE, "考勤记录不存在");
        }


        LocalDateTime checkOutTimeLocal = LocalDateTime.now();
        LocalTime checkOutTime = checkOutTimeLocal.toLocalTime();

        if (record.getCheckInTime() == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "当前考勤记录缺少上班打卡时间");
        }
        if (record.getCheckOutTime() != null) {
            throw new BusinessException(BAD_REQUEST_CODE, "当前考勤记录已下班打卡");
        }


        record.setCheckOutTime(checkOutTimeLocal);
        record.setWorkHours(calculateWorkHours(record.getCheckInTime(), checkOutTimeLocal));
        record.setStatus(checkOutTime.isAfter(CheckInOutTime.checkOutTime) ? 1 : 2);
        attendanceRecordMapper.updateById(record);

        // 修改员工状态为下班中
        // 先根据考勤记录关联的员工ID获取员工对象
        Employee employee = employeeMapper.selectById(record.getEmployeeId());
        if (employee == null) {
            throw new BusinessException(NOT_FOUND_CODE, "当前考勤记录关联的员工不存在");
        }
        // 修改员工状态为下班中
        employee.setStatus(0);
        employeeMapper.updateById(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCheckRecord(Integer attendanceId) {
        if (attendanceId == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "考勤记录ID不能为空");
        }
        AttendanceRecord record = attendanceRecordMapper.selectById(attendanceId);
        if (record == null) {
            throw new BusinessException(NOT_FOUND_CODE, "考勤记录不存在");
        }
        attendanceRecordMapper.deleteById(attendanceId);
    }

    @Override
    public PageBean getCheckRecordList(int pageNum, int pageSize, String employeeNumber, String date, String checkinTime, String checkoutTime, Integer status, Integer workHours) {
        Integer employeeId = null;
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            employeeId = findEmployeeByNumber(employeeNumber).getEmployeeId();
        }

        QueryWrapper<AttendanceRecord> wrapper = new QueryWrapper<>();
        wrapper.eq(employeeId != null, "employee_id", employeeId)
                .eq(date != null && !date.isBlank(), "date", date)
                .eq(status != null, "status", status)
                .orderByDesc("attendance_id");

        List<AttendanceRecord> records = attendanceRecordMapper.selectList(wrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (AttendanceRecord record : records) {
            if (checkinTime != null && record.getCheckInTime() != null
                    && !record.getCheckInTime().toString().contains(checkinTime)) {
                continue;
            }
            if (checkoutTime != null && record.getCheckOutTime() != null
                    && !record.getCheckOutTime().toString().contains(checkoutTime)) {
                continue;
            }
            if (workHours != null && record.getWorkHours() != null
                    && record.getWorkHours().intValue() != workHours) {
                continue;
            }
            resultList.add(buildAttendanceItem(record));
        }

        return buildPageBean(resultList, pageNum, pageSize);
    }

    private Employee resolveEmployeeByNumber(Map<String, Object> attendance) {
        return findEmployeeByNumber(requireEmployeeNumber(attendance));
    }

    private String requireEmployeeNumber(Map<String, Object> attendance) {
        return requireString(attendance, "员工工号", "employeeNumber");
    }

    private Employee findEmployeeByNumber(String employeeNumber) {
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.eq("employee_number", employeeNumber);
        Employee employee = employeeMapper.selectOne(wrapper);
        if (employee == null) {
            throw new BusinessException(NOT_FOUND_CODE, "员工不存在");
        }
        return employee;
    }

    private BigDecimal calculateWorkHours(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        long minutes = Duration.between(checkInTime, checkOutTime).toMinutes();
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> buildAttendanceItem(AttendanceRecord record) {
        Employee employee = employeeMapper.selectById(record.getEmployeeId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("attendanceID", record.getAttendanceId());
        result.put("employeeNumber", employee == null ? null : employee.getEmployeeNumber());
        result.put("employeeName", employee == null ? null : employee.getName());
        result.put("date", record.getDate());
        result.put("checkinTime", record.getCheckInTime());
        result.put("checkoutTime", record.getCheckOutTime());
        result.put("status", record.getStatus());
        result.put("workHours", record.getWorkHours());
        return result;
    }
}
