package com.example.smartfactoryservice.Mapper.ProductModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductStore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品出入库表 Mapper
 */
@Mapper
public interface ProductStoreMapper extends BaseMapper<ProductStore> {
}
