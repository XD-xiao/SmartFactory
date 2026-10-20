package com.example.smartfactoryservice.Service.ProductModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroup;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduction;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduct;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductionMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductMapper;
import com.example.smartfactoryservice.Service.ProductModule.ProductProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductProductionServiceImpl extends ProductServiceSupport implements ProductProductionService {

    private final ProductProductionMapper productProductionMapper;
    private final ProductProductMapper productProductMapper;
    private final DeviceGroupMapper deviceGroupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setProduction(Map<String, Object> production) {
        requireBody(production, "产量信息不能为空");

        String barCode = requireString(production, "产品编号", "barCode");
        // 根据条形码查找产品
        ProductProduct product = productProductMapper.selectOne(
                new QueryWrapper<ProductProduct>().eq("bar_code", barCode).eq("delete_flag", 0));
        if (product == null) {
            throw new BusinessException(NOT_FOUND_CODE, "产品不存在");
        }
        Long productId = product.getId();

        Long groupId = getLong(production, "groupId");
        // 同产品ID且同分组的产量记录不允许重复添加
        Long existCount = productProductionMapper.selectCount(
                new QueryWrapper<ProductProduction>().eq("product_id", productId).eq("group_id", groupId));
        if (existCount > 0) {
            throw new BusinessException(NOT_FOUND_CODE, "该产品在当前分组下已存在产量记录，无法重复添加");
        }

        ProductProduction entity = new ProductProduction();
        entity.setProductId(productId);
        entity.setPlanName(requireString(production, "计划名称", "planName"));
        entity.setPlannedProduction(requireInteger(production, "计划产量", "plannedProduction"));
        entity.setActualProduction(0);
        entity.setStoreNum(0);
        entity.setQualifiedNum(0);
        entity.setGroupId(groupId);

        productProductionMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduction(Map<String, Object> production) {
        requireBody(production, "产量信息不能为空");

        Long id = requireLong(production, "产量记录ID", "id");
        ProductProduction entity = productProductionMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "产量记录不存在");
        }

        String planName = getString(production, "planName");
        if (planName != null) {
            entity.setPlanName(planName);
        }

        Integer plannedProduction = getInteger(production, "plannedProduction");
        if (plannedProduction != null) {
            entity.setPlannedProduction(plannedProduction);
        }

        Integer storeNum = getInteger(production, "storeNum");
        if (storeNum != null) {
            entity.setStoreNum(storeNum);
        }

        Integer qualifiedNum = getInteger(production, "qualifiedNum");
        if (qualifiedNum != null) {
            entity.setQualifiedNum(qualifiedNum);
        }

        Long groupId = getLong(production, "groupId");
        if (groupId != null) {
            entity.setGroupId(groupId);
        }

        productProductionMapper.updateById(entity);
    }

    @Override
    public PageBean getProductionList(int pageNum, int pageSize, String barCode, String planName) {
        QueryWrapper<ProductProduction> wrapper = new QueryWrapper<>();

        // 如果传了条形码，则根据barCode查找productId再筛选
        if (barCode != null && !barCode.isBlank()) {
            ProductProduct product = productProductMapper.selectOne(
                    new QueryWrapper<ProductProduct>().eq("bar_code", barCode).eq("delete_flag", 0));
            if (product == null) {
                return buildPageBean(new ArrayList<>(), pageNum, pageSize);
            }
            wrapper.eq("product_id", product.getId());
        }

        // 按计划名称模糊搜索
        if (planName != null && !planName.isBlank()) {
            wrapper.like("plan_name", planName);
        }

        wrapper.orderByDesc("id");

        List<ProductProduction> productions = productProductionMapper.selectList(wrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (ProductProduction production : productions) {
            resultList.add(buildProductionDetail(production));
        }
        return buildPageBean(resultList, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void qualityCheck(Map<String, Object> params) {
        requireBody(params, "质检参数不能为空");

        String barCode = requireString(params, "产品编号", "barCode");
//        String energyLevel = requireString(params, "产品状态", "energyLevel");


        // 根据条形码查找产品
        ProductProduct product = productProductMapper.selectOne(
                new QueryWrapper<ProductProduct>().eq("bar_code", barCode).eq("delete_flag", 0));
        if (product == null) {
            throw new BusinessException(NOT_FOUND_CODE, "产品不存在");
        }
        Long productId = product.getId();

        Long groupId = requireLong(params, "分组ID", "groupId");

        // 根据 productId 和 groupId 查找产量记录
        ProductProduction production = productProductionMapper.selectOne(
                new QueryWrapper<ProductProduction>()
                        .eq("product_id", productId)
                        .eq("group_id", groupId));
        if (production == null) {
            throw new BusinessException(NOT_FOUND_CODE, "未找到对应的产量记录");
        }

        // 质检合格数已达入库数量，不允许继续质检
        if (production.getQualifiedNum() >= production.getStoreNum()) {
            throw new BusinessException(NOT_FOUND_CODE, "超出入库数量");
        }

        production.setQualifiedNum(production.getQualifiedNum() + 1);
        productProductionMapper.updateById(production);
    }

    private Map<String, Object> buildProductionDetail(ProductProduction production) {
        ProductProduct product = productProductMapper.selectById(production.getProductId());

        // 根据分组ID查询分组名称
        String groupName = null;
        if (production.getGroupId() != null) {
            DeviceGroup deviceGroup = deviceGroupMapper.selectById(production.getGroupId());
            groupName = deviceGroup != null ? deviceGroup.getName() : null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", production.getId());
        result.put("productId", production.getProductId());
        result.put("planName", production.getPlanName());
        result.put("barCode", product != null ? product.getBarCode() : null);
        result.put("productCode", product != null ? product.getProductCode() : null);
        result.put("plannedProduction", production.getPlannedProduction());
        result.put("actualProduction", production.getActualProduction());
        result.put("storeNum", production.getStoreNum());
        result.put("qualifiedNum", production.getQualifiedNum());
        result.put("groupId", production.getGroupId());
        result.put("groupName", groupName);
        result.put("createTime", production.getCreateTime());
        result.put("deleteFlag", product != null ? product.isDeleteFlag() : null);
        return result;
    }
}
