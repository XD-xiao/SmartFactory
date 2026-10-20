package com.example.smartfactoryservice.Mapper.ProductModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品型号表 Mapper
 */
@Mapper
public interface ProductModelMapper extends BaseMapper<ProductModel> {
}
