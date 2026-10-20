package com.example.smartfactoryservice.Mapper.ProductModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品类型表 Mapper
 */
@Mapper
public interface ProductTypeMapper extends BaseMapper<ProductType> {
}
