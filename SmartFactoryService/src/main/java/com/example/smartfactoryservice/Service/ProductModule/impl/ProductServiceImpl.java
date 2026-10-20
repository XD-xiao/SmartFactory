package com.example.smartfactoryservice.Service.ProductModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduct;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductType;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductModel;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductTypeMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductModelMapper;
import com.example.smartfactoryservice.Service.ProductModule.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ProductServiceSupport implements ProductService {

    private final ProductProductMapper productProductMapper;
    private final ProductTypeMapper productTypeMapper;
    private final ProductModelMapper productModelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProduct(Map<String, Object> product) {
        requireBody(product, "产品信息不能为空");

        ProductProduct entity = new ProductProduct();
        entity.setTypeCode(requireString(product, "产品类型码", "typeCode"));
        entity.setModelCode(requireString(product, "产品型号码", "modelCode"));

        // 校验类型码和型号码是否存在
        ensureTypeExists(entity.getTypeCode());
        ensureModelExists(entity.getModelCode());

        // 自动生成条形码编号
        String barCode = getString(product, "barCode");
        if (barCode == null || barCode.isBlank()) {
            barCode = "BAR" + entity.getTypeCode() + entity.getModelCode() + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        }
        entity.setBarCode(barCode);

        entity.setProductCode(getString(product, "productCode"));
        entity.setPicId(getLong(product, "picId"));
        entity.setDeviceSn(getString(product, "deviceSn"));

        productProductMapper.insert(entity);
    }

    @Override
    public PageBean getProductList(int pageNum, int pageSize, String barCode, String typeCode, String modelCode, String productCode, String deviceSn) {
        QueryWrapper<ProductProduct> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0)
                .eq(barCode != null && !barCode.isBlank(), "bar_code", barCode)
                .eq(typeCode != null && !typeCode.isBlank(), "type_code", typeCode)
                .eq(modelCode != null && !modelCode.isBlank(), "model_code", modelCode)
                .eq(productCode != null && !productCode.isBlank(), "product_code", productCode)
                .eq(deviceSn != null && !deviceSn.isBlank(), "device_sn", deviceSn)
                .orderByDesc("id");

        List<ProductProduct> products = productProductMapper.selectList(wrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (ProductProduct p : products) {
            resultList.add(buildProductDetail(p));
        }
        return buildPageBean(resultList, pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getProduct(Integer id, String barCode) {
        ProductProduct product;
        if (id != null) {
            product = productProductMapper.selectById(id);
        } else if (barCode != null && !barCode.isBlank()) {
            QueryWrapper<ProductProduct> wrapper = new QueryWrapper<>();
            wrapper.eq("bar_code", barCode).eq("delete_flag", 0);
            product = productProductMapper.selectOne(wrapper);
        } else {
            throw new BusinessException(BAD_REQUEST_CODE, "产品ID或条形码编号不能为空");
        }

        if (product == null || product.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "产品不存在");
        }
        return buildProductDetail(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Map<String, Object> product) {
        requireBody(product, "产品信息不能为空");

        Long id = requireLong(product, "产品ID", "id");
        ProductProduct entity = productProductMapper.selectById(id);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "产品不存在");
        }

        String barCode = getString(product, "barCode");
        if (barCode != null) {
            // 检查条形码唯一性（排除自身）
            QueryWrapper<ProductProduct> wrapper = new QueryWrapper<>();
            wrapper.eq("bar_code", barCode).ne("id", id).eq("delete_flag", 0);
            if (productProductMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(BAD_REQUEST_CODE, "条形码编号已存在");
            }
            entity.setBarCode(barCode);
        }

        String typeCode = getString(product, "typeCode");
        if (typeCode != null) {
            ensureTypeExists(typeCode);
            entity.setTypeCode(typeCode);
        }

        String modelCode = getString(product, "modelCode");
        if (modelCode != null) {
            ensureModelExists(modelCode);
            entity.setModelCode(modelCode);
        }

        String pCode = getString(product, "productCode");
        if (pCode != null) {
            entity.setProductCode(pCode);
        }

        Long picId = getLong(product, "picId");
        if (picId != null) {
            entity.setPicId(picId);
        }

        String deviceSn = getString(product, "deviceSn");
        if (deviceSn != null) {
            entity.setDeviceSn(deviceSn);
        }

        productProductMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Integer id) {
        if (id == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "产品ID不能为空");
        }
        ProductProduct entity = productProductMapper.selectById(id);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "产品不存在");
        }
        entity.setDeleteFlag(true);
        productProductMapper.updateById(entity);
    }

    private void ensureTypeExists(String typeCode) {
        QueryWrapper<ProductType> wrapper = new QueryWrapper<>();
        wrapper.eq("code", typeCode)
                .eq("delete_flag", 0);
        if (productTypeMapper.selectCount(wrapper) == 0) {
            throw new BusinessException(NOT_FOUND_CODE, "产品类型不存在");
        }
    }

    private void ensureModelExists(String modelCode) {
        QueryWrapper<ProductModel> wrapper = new QueryWrapper<>();
        wrapper.eq("code", modelCode)
                .eq("delete_flag", 0);
        if (productModelMapper.selectCount(wrapper) == 0) {
            throw new BusinessException(NOT_FOUND_CODE, "产品型号不存在");
        }
    }

    private Map<String, Object> buildProductDetail(ProductProduct product) {
        String typeName = null;
        if (product.getTypeCode() != null) {
            QueryWrapper<ProductType> typeWrapper = new QueryWrapper<>();
            typeWrapper.eq("code", product.getTypeCode())
                    .eq("delete_flag", 0);
            ProductType type = productTypeMapper.selectOne(typeWrapper);
            typeName = type != null ? type.getName() : null;
        }

        String modelName = null;
        if (product.getModelCode() != null) {
            QueryWrapper<ProductModel> modelWrapper = new QueryWrapper<>();
            modelWrapper.eq("code", product.getModelCode())
                    .eq("delete_flag", 0);
            ProductModel model = productModelMapper.selectOne(modelWrapper);
            modelName = model != null ? model.getName() : null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", product.getId());
        result.put("barCode", product.getBarCode());
        result.put("typeCode", product.getTypeCode());
        result.put("typeName", typeName);
        result.put("modelCode", product.getModelCode());
        result.put("modelName", modelName);
        result.put("productCode", product.getProductCode());
        result.put("picId", product.getPicId());
        result.put("deviceSn", product.getDeviceSn());
        result.put("createTime", product.getCreateTime());
        result.put("updateTime", product.getUpdateTime());
        result.put("deleteFlag", product.isDeleteFlag());
        return result;
    }
}
