package com.example.smartfactoryservice.Mapper.PersonManagementModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工表 Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
