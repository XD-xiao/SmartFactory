package com.example.smartfactoryservice.Service.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface LeaveService {

    void addLeaveRecord(Map<String, Object> leaveRecord);

    void updateLeaveRecord(Map<String, Object> leaveRecord);

    PageBean getLeaveList(int pageNum, int pageSize, String employeeNumber, Integer leaveType, Integer status, String startDate, String endDate);

    void deleteLeave(Integer leaveId);
}
