package com.example.smartfactoryservice.Mapper.PersonManagementModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.LeaveRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 请假记录表 Mapper
 */
@Mapper
public interface LeaveRecordMapper extends BaseMapper<LeaveRecord> {
}
