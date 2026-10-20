package com.example.smartfactoryservice.Mapper.PersonManagementModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Position;
import org.apache.ibatis.annotations.Mapper;

/**
 * 职位表 Mapper
 */
@Mapper
public interface PositionMapper extends BaseMapper<Position> {
}
