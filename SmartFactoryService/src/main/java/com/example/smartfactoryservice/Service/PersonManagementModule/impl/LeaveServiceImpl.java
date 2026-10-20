package com.example.smartfactoryservice.Service.PersonManagementModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.LeaveRecord;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.EmployeeMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.LeaveRecordMapper;
import com.example.smartfactoryservice.Service.PersonManagementModule.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl extends PersonManagementServiceSupport implements LeaveService {

    private final LeaveRecordMapper leaveRecordMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addLeaveRecord(Map<String, Object> leaveRecord) {
        requireBody(leaveRecord, "请假信息不能为空");

        Employee employee = findEmployeeByNumber(requireEmployeeNumber(leaveRecord));
        if (employee.isDeleteFlag()) {
            throw new BusinessException(BAD_REQUEST_CODE, "员工已被删除，无法添加请假记录");
        }
        Integer employeeId = employee.getEmployeeId();

        LeaveRecord entity = new LeaveRecord();
        entity.setEmployeeId(employeeId);
        entity.setLeaveType(requireInteger(leaveRecord, "请假类型", "leaveType"));
        entity.setStartDate(requireLocalDate(leaveRecord, "开始日期", "startDate"));
        entity.setEndDate(requireLocalDate(leaveRecord, "结束日期", "endDate"));
        if (entity.getEndDate().isBefore(entity.getStartDate())) {
            throw new BusinessException(BAD_REQUEST_CODE, "结束日期不能早于开始日期");
        }

        entity.setDays(calculateLeaveDays(entity.getStartDate(), entity.getEndDate()));
        entity.setReason(requireString(leaveRecord, "请假原因", "reason"));
        entity.setStatus(0);
        leaveRecordMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLeaveRecord(Map<String, Object> leaveRecord) {
        requireBody(leaveRecord, "请假信息不能为空");

        Integer leaveId = requireInteger(leaveRecord, "请假记录ID", "leave_id");
        LeaveRecord entity = leaveRecordMapper.selectById(leaveId);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "请假记录不存在");
        }
        if (entity.getStatus() != null && entity.getStatus() != 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "仅待审批状态的请假记录可修改");
        }

        Integer leaveType = getInteger(leaveRecord, "leave_type");
        if (leaveType != null) {
            entity.setLeaveType(leaveType);
        }

        if (getLocalDate(leaveRecord, "start_date") != null) {
            entity.setStartDate(getLocalDate(leaveRecord, "start_date"));
        }
        if (getLocalDate(leaveRecord, "end_date") != null) {
            entity.setEndDate(getLocalDate(leaveRecord, "end_date"));
        }
        if (entity.getEndDate().isBefore(entity.getStartDate())) {
            throw new BusinessException(BAD_REQUEST_CODE, "结束日期不能早于开始日期");
        }

        BigDecimal days = getBigDecimal(leaveRecord, "days");
        entity.setDays(days == null ? calculateLeaveDays(entity.getStartDate(), entity.getEndDate()) : days);

        String reason = getString(leaveRecord, "reason");
        if (reason != null) {
            entity.setReason(reason);
        }

        Integer status = getInteger(leaveRecord, "status");
        if (status != null) {
            entity.setStatus(status);
        }

        leaveRecordMapper.updateById(entity);
    }

    @Override
    public PageBean getLeaveList(int pageNum, int pageSize, String employeeNumber, Integer leaveType, Integer status, String startDate, String endDate) {
        Integer employeeId = null;
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            employeeId = findEmployeeByNumber(employeeNumber).getEmployeeId();
        }

        QueryWrapper<LeaveRecord> wrapper = new QueryWrapper<>();
        wrapper.eq(employeeId != null, "employee_id", employeeId)
                .eq(leaveType != null, "leave_type", leaveType)
                .eq(status != null, "status", status)
                .ge(startDate != null && !startDate.isBlank(), "start_date", startDate)
                .le(endDate != null && !endDate.isBlank(), "end_date", endDate)
                .orderByDesc("leave_id");

        List<LeaveRecord> leaveRecords = leaveRecordMapper.selectList(wrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (LeaveRecord record : leaveRecords) {
            resultList.add(buildLeaveItem(record));
        }
        return buildPageBean(resultList, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLeave(Integer leaveId) {
        if (leaveId == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "请假记录ID不能为空");
        }
        LeaveRecord entity = leaveRecordMapper.selectById(leaveId);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "请假记录不存在");
        }
        if (entity.getStatus() != null && entity.getStatus() != 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "仅待审批状态的请假记录可删除");
        }
        leaveRecordMapper.deleteById(leaveId);
    }

    private String requireEmployeeNumber(Map<String, Object> leaveRecord) {
        return requireString(leaveRecord, "员工工号", "employeeNumber");
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

    private BigDecimal calculateLeaveDays(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return BigDecimal.valueOf(days);
    }

    private Map<String, Object> buildLeaveItem(LeaveRecord record) {
        Employee employee = employeeMapper.selectById(record.getEmployeeId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("leaveID", record.getLeaveId());
        result.put("employeeID", record.getEmployeeId());
        result.put("employeeNumber", employee == null ? null : employee.getEmployeeNumber());
        result.put("employeeName", employee == null ? null : employee.getName());
        result.put("leaveType", record.getLeaveType());
        result.put("status", record.getStatus());
        result.put("startDate", record.getStartDate());
        result.put("endDate", record.getEndDate());
        result.put("days", record.getDays());
        result.put("reason", record.getReason());
        return result;
    }
}
