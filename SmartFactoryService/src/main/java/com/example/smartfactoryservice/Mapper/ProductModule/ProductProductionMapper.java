package com.example.smartfactoryservice.Mapper.ProductModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品产量表 Mapper
 */
@Mapper
public interface ProductProductionMapper extends BaseMapper<ProductProduction> {
}
