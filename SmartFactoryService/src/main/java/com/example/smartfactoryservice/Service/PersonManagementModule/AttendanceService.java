package com.example.smartfactoryservice.Service.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface AttendanceService {

    void addCheckRecord(Map<String, Object> attendance);

    void updateCheckRecord(Integer attendanceID);

    void deleteCheckRecord(Integer attendanceId);

    PageBean getCheckRecordList(int pageNum, int pageSize, String employeeNumber, String date, String checkinTime, String checkoutTime, Integer status, Integer workHours);
}
