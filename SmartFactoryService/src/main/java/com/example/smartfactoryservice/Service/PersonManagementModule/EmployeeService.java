package com.example.smartfactoryservice.Service.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface EmployeeService {

    String accessControl(Map<String, Object> accessInfo);

    String addEmployee(Map<String, Object> employee);

    Map<String, Object> login(Map<String, Object> loginInfo);

    PageBean getEmployeeList(int pageNum, int pageSize, String keyword, Integer departmentId, Integer positionId, Integer status);

    Map<String, Object> getEmployee(String employeeNumber);

    void updateEmployee(Map<String, Object> employee);

    void updatePassword(Map<String, Object> passwordInfo);

    void deleteEmployee(Integer employeeId);
}
