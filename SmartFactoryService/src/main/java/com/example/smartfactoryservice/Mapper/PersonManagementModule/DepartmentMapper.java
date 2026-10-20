package com.example.smartfactoryservice.Mapper.PersonManagementModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Department;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门表 Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
}
