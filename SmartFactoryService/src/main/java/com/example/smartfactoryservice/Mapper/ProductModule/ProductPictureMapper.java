package com.example.smartfactoryservice.Mapper.ProductModule;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductPicture;
import org.apache.ibatis.annotations.Mapper;

/**
 * 产品图片表 Mapper
 */
@Mapper
public interface ProductPictureMapper extends BaseMapper<ProductPicture> {
}
