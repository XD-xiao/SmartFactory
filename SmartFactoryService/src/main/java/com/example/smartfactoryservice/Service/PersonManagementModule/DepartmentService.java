package com.example.smartfactoryservice.Service.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface DepartmentService {

    void addDepartment(Map<String, Object> department);

    PageBean getDepartmeList(int pageNum, int pageSize, Integer departmentId, String departmentName, Integer status, Integer sort);

    void deleteDepartment(Integer departmentId);

    void updateDepartment(Map<String, Object> department);
}
