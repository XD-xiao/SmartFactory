package com.example.smartfactoryservice.Service.ProductModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductPicture;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductPictureMapper;
import com.example.smartfactoryservice.Service.ProductModule.ProductPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductPictureServiceImpl extends ProductServiceSupport implements ProductPictureService {

    private final ProductPictureMapper productPictureMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadPicture(Map<String, Object> picture) {
        requireBody(picture, "图片信息不能为空");

        ProductPicture entity = new ProductPicture();
        entity.setContent(requireString(picture, "图片内容", "content"));
        productPictureMapper.insert(entity);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", entity.getId());
        return result;
    }

    @Override
    public Map<String, Object> getPicture(Integer id) {
        if (id == null) {
            // 如果不传id，返回最新一张
            QueryWrapper<ProductPicture> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("id").last("LIMIT 1");
            ProductPicture picture = productPictureMapper.selectOne(wrapper);
            return buildPictureResult(picture);
        }
        ProductPicture picture = productPictureMapper.selectById(id);
        return buildPictureResult(picture);
    }

    private Map<String, Object> buildPictureResult(ProductPicture picture) {
        if (picture == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", picture.getId());
        result.put("content", picture.getContent());
        result.put("createTime", picture.getCreateTime());
        return result;
    }
}
