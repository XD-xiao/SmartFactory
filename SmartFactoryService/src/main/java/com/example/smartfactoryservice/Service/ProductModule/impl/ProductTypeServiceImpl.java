package com.example.smartfactoryservice.Service.ProductModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductType;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduct;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductTypeMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductMapper;
import com.example.smartfactoryservice.Service.ProductModule.ProductTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductTypeServiceImpl extends ProductServiceSupport implements ProductTypeService {

    private final ProductTypeMapper productTypeMapper;
    private final ProductProductMapper productProductMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addType(Map<String, Object> type) {
        requireBody(type, "产品类型信息不能为空");

        ProductType entity = new ProductType();
        entity.setName(requireString(type, "类型名称", "name"));
        entity.setCode(requireString(type, "类型码", "code"));

        // 检查类型码唯一性
        QueryWrapper<ProductType> wrapper = new QueryWrapper<>();
        wrapper.eq("code", entity.getCode())
                .eq("delete_flag", 0);
        if (productTypeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "类型码已存在");
        }

        productTypeMapper.insert(entity);
    }

    @Override
    public PageBean getTypeList(int pageNum, int pageSize, String name, String code) {
        QueryWrapper<ProductType> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0)
                .like(name != null && !name.isBlank(), "name", name)
                .eq(code != null && !code.isBlank(), "code", code)
                .orderByDesc("id");

        List<ProductType> types = productTypeMapper.selectList(wrapper);
        return buildPageBean(types, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(Map<String, Object> type) {
        requireBody(type, "产品类型信息不能为空");

        Long id = requireLong(type, "类型ID", "id");
        QueryWrapper<ProductType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("delete_flag", 0);
        ProductType entity = productTypeMapper.selectOne(queryWrapper);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "产品类型不存在");
        }

        String name = getString(type, "name");
        if (name != null) {
            entity.setName(name);
        }

        String code = getString(type, "code");
        if (code != null) {
            // 检查类型码唯一性（排除自身，跳过已删除）
            QueryWrapper<ProductType> wrapper = new QueryWrapper<>();
            wrapper.eq("code", code).ne("id", id)
                    .eq("delete_flag", 0);
            if (productTypeMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(BAD_REQUEST_CODE, "类型码已存在");
            }
            entity.setCode(code);
        }

        productTypeMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Integer id) {
        if (id == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "类型ID不能为空");
        }

        ProductType entity = productTypeMapper.selectById(id);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "产品类型不存在");
        }

        // 检查是否有未删除的产品关联该类型
        QueryWrapper<ProductProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("type_code", entity.getCode())
                .eq("delete_flag", 0);
        long count = productProductMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "该类型下还有 " + count + " 个产品，无法删除");
        }

        entity.setDeleteFlag(true);
        productTypeMapper.updateById(entity);
    }
}
