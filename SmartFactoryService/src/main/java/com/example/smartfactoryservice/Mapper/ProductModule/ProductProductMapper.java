package com.example.smartfactoryservice.Mapper.ProductModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品表 Mapper
 */
@Mapper
public interface ProductProductMapper extends BaseMapper<ProductProduct> {
}
