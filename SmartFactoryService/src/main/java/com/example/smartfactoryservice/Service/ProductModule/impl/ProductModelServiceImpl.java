package com.example.smartfactoryservice.Service.ProductModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductModel;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduct;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductModelMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductMapper;
import com.example.smartfactoryservice.Service.ProductModule.ProductModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductModelServiceImpl extends ProductServiceSupport implements ProductModelService {

    private final ProductModelMapper productModelMapper;
    private final ProductProductMapper productProductMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addModel(Map<String, Object> model) {
        requireBody(model, "产品型号信息不能为空");

        ProductModel entity = new ProductModel();
        entity.setName(requireString(model, "型号名称", "name"));
        entity.setCode(requireString(model, "型号码", "code"));

        // 检查型号码唯一性
        QueryWrapper<ProductModel> wrapper = new QueryWrapper<>();
        wrapper.eq("code", entity.getCode())
                .eq("delete_flag", 0);
        if (productModelMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "型号码已存在");
        }

        productModelMapper.insert(entity);
    }

    @Override
    public PageBean getModelList(int pageNum, int pageSize, String name, String code) {
        QueryWrapper<ProductModel> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0)
                .like(name != null && !name.isBlank(), "name", name)
                .eq(code != null && !code.isBlank(), "code", code)
                .orderByDesc("id");

        List<ProductModel> models = productModelMapper.selectList(wrapper);
        return buildPageBean(models, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModel(Map<String, Object> model) {
        requireBody(model, "产品型号信息不能为空");

        Long id = requireLong(model, "型号ID", "id");
        QueryWrapper<ProductModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("delete_flag", 0);
        ProductModel entity = productModelMapper.selectOne(queryWrapper);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "产品型号不存在");
        }

        String name = getString(model, "name");
        if (name != null) {
            entity.setName(name);
        }

        String code = getString(model, "code");
        if (code != null) {
            // 检查型号码唯一性（排除自身，跳过已删除）
            QueryWrapper<ProductModel> wrapper = new QueryWrapper<>();
            wrapper.eq("code", code).ne("id", id)
                    .eq("delete_flag", 0);
            if (productModelMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(BAD_REQUEST_CODE, "型号码已存在");
            }
            entity.setCode(code);
        }

        productModelMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Integer id) {
        if (id == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "型号ID不能为空");
        }

        ProductModel entity = productModelMapper.selectById(id);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "产品型号不存在");
        }

        // 检查是否有未删除的产品关联该型号
        QueryWrapper<ProductProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("model_code", entity.getCode())
                .eq("delete_flag", 0);
        long count = productProductMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "该型号下还有 " + count + " 个产品，无法删除");
        }

        entity.setDeleteFlag(true);
        productModelMapper.updateById(entity);
    }
}
