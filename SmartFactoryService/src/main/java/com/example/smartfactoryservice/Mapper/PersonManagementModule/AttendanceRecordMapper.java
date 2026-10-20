package com.example.smartfactoryservice.Mapper.PersonManagementModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.AttendanceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤记录表 Mapper
 */
@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
}
