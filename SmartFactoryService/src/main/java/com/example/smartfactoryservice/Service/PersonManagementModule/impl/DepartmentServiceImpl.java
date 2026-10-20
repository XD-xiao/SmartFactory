package com.example.smartfactoryservice.Service.PersonManagementModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Department;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.DepartmentMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.EmployeeMapper;
import com.example.smartfactoryservice.Service.PersonManagementModule.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends PersonManagementServiceSupport implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDepartment(Map<String, Object> department) {
        requireBody(department, "部门信息不能为空");

        Department entity = new Department();
        entity.setDepartmentName(requireString(department, "部门名称", "departmentName"));
        entity.setStatus(requireInteger(department, "部门状态", "status"));
        entity.setRemark(getString(department, "remark"));
        departmentMapper.insert(entity);
    }

    @Override
    public PageBean getDepartmeList(int pageNum, int pageSize, Integer departmentId, String departmentName, Integer status, Integer sort) {
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0)
                .eq(departmentId != null, "department_id", departmentId)
                .like(departmentName != null && !departmentName.isBlank(), "department_name", departmentName)
                .eq(status != null, "status", status);

        if (sort != null && sort == 1) {
            wrapper.orderByAsc("department_id");
        } else {
            wrapper.orderByDesc("department_id");
        }

        List<Department> departments = departmentMapper.selectList(wrapper);
        return buildPageBean(departments, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Integer departmentId) {
        if (departmentId == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "部门ID不能为空");
        }
        Department entity = departmentMapper.selectById(departmentId);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "部门不存在");
        }
        // 查询部门下是否有未删除的员工关联
        QueryWrapper<com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee> wrapper = new QueryWrapper<>();
        wrapper.eq("department_id", departmentId)
                .eq("delete_flag", 0);
        long count = employeeMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "该部门下还有 " + count + " 名员工，无法删除");
        }
        entity.setDeleteFlag(true);
        departmentMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(Map<String, Object> department) {
        requireBody(department, "部门信息不能为空");

        Integer departmentId = requireInteger(department, "部门ID", "departmentId");
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("department_id", departmentId)
                .eq("delete_flag", 0);
        Department entity = departmentMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "部门不存在");
        }

        entity.setDepartmentName(requireString(department, "部门名称", "departmentName"));
        entity.setStatus(requireInteger(department, "部门状态", "status"));
        entity.setRemark(getString(department, "remark"));
        departmentMapper.updateById(entity);
    }
}
