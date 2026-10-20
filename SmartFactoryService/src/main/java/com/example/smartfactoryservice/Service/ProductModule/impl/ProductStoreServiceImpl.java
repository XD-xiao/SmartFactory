package com.example.smartfactoryservice.Service.ProductModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroup;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroupDevice;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduct;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductProduction;
import com.example.smartfactoryservice.Domain.Entity.ProductModule.ProductStore;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupDeviceMapper;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.EmployeeMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductProductionMapper;
import com.example.smartfactoryservice.Mapper.ProductModule.ProductStoreMapper;
import com.example.smartfactoryservice.Service.ProductModule.ProductStoreService;
import com.example.smartfactoryservice.Utils.UserContext;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductStoreServiceImpl extends ProductServiceSupport implements ProductStoreService {

    private final ProductStoreMapper productStoreMapper;
    private final ProductProductMapper productProductMapper;
    private final ProductProductionMapper productProductionMapper;
    private final EmployeeMapper employeeMapper;
    private final DeviceGroupDeviceMapper deviceGroupDeviceMapper;
    private final DeviceGroupMapper deviceGroupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void storeIn(Map<String, Object> storeInfo) {
        requireBody(storeInfo, "入库信息不能为空");

        String barCode = requireString(storeInfo, "条形码", "barCode");
        // 根据barCode查找产品
        ProductProduct product = productProductMapper.selectOne(
                new QueryWrapper<ProductProduct>().eq("bar_code", barCode).eq("delete_flag", 0));
        if (product == null) {
            throw new BusinessException(NOT_FOUND_CODE, "产品不存在");
        }
        Long productId = product.getId();

        // 根据设备SN查找所在分组
        String deviceSn = getString(storeInfo, "deviceSn");
        if (deviceSn == null || deviceSn.isBlank()) {
            throw new BusinessException(NOT_FOUND_CODE, "设备SN不能为空");
        }
        Long groupId = findGroupIdByDeviceSn(deviceSn);
        if (groupId == null) {
            throw new BusinessException(NOT_FOUND_CODE, "设备未加入任何分组，无法入库");
        }

        // 校验产品产量表：按产品ID+分组查产量记录，入库数量须小于计划数量
        ProductProduction production = productProductionMapper.selectOne(
                new QueryWrapper<ProductProduction>().eq("product_id", productId).eq("group_id", groupId));
        if (production == null) {
            throw new BusinessException(NOT_FOUND_CODE, "该产品在当前分组下无产量记录，无法入库");
        }
        int storeNum = production.getStoreNum() != null ? production.getStoreNum() : 0;
        int plannedProduction = production.getPlannedProduction() != null ? production.getPlannedProduction() : 0;
        if (storeNum >= plannedProduction) {
            throw new BusinessException(NOT_FOUND_CODE, "当前入库数量已达到计划数量，无法继续入库");
        }

        ProductStore entity = new ProductStore();
        entity.setProductId(productId);
        entity.setAction(1); // 1:入库
        entity.setDeviceSn(deviceSn);
        entity.setGroupId(groupId);

        // 根据员工工号查找员工ID
        Map<String, Object> userMap = UserContext.getUser();
        Object empObj = userMap.get("employeeNumber");
        String employeeNumber = null;
        if (empObj instanceof Number) {
            employeeNumber = String.valueOf(((Number) empObj).longValue());
        } else if (empObj != null) {
            employeeNumber = empObj.toString();
        }
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            Long employeeId = findEmployeeIdByNumber(employeeNumber);
            entity.setEmployeeId(employeeId);
        }

        entity.setDatetime(LocalDateTime.now());
        productStoreMapper.insert(entity);

        // 入库后 storeNum +1
        production.setStoreNum(storeNum + 1);
        productProductionMapper.updateById(production);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void storeOut(Map<String, Object> storeInfo) {
        requireBody(storeInfo, "出库信息不能为空");

        String barCode = requireString(storeInfo, "条形码", "barCode");
        // 根据barCode查找产品
        ProductProduct product = productProductMapper.selectOne(
                new QueryWrapper<ProductProduct>().eq("bar_code", barCode).eq("delete_flag", 0));
        if (product == null) {
            throw new BusinessException(NOT_FOUND_CODE, "产品不存在");
        }
        Long productId = product.getId();

        // 根据设备SN查找所在分组
        String deviceSn = getString(storeInfo, "deviceSn");
        if (deviceSn == null || deviceSn.isBlank()) {
            throw new BusinessException(NOT_FOUND_CODE, "设备SN不能为空");
        }
        Long groupId = findGroupIdByDeviceSn(deviceSn);
        if (groupId == null) {
            throw new BusinessException(NOT_FOUND_CODE, "设备未加入任何分组，无法出库");
        }

        // 校验产品产量表：按产品ID+分组查产量记录，实际产量须小于合格数量
        ProductProduction production = productProductionMapper.selectOne(
                new QueryWrapper<ProductProduction>().eq("product_id", productId).eq("group_id", groupId));
        if (production == null) {
            throw new BusinessException(NOT_FOUND_CODE, "该产品在当前分组下无产量记录，无法出库");
        }
        int actualProduction = production.getActualProduction() != null ? production.getActualProduction() : 0;
        int qualifiedNum = production.getQualifiedNum() != null ? production.getQualifiedNum() : 0;
        if (actualProduction >= qualifiedNum) {
            throw new BusinessException(NOT_FOUND_CODE, "当前实际产量已达到合格数量，无法继续出库");
        }

        ProductStore entity = new ProductStore();
        entity.setProductId(productId);
        entity.setAction(0); // 0:出库
        entity.setDeviceSn(deviceSn);
        entity.setGroupId(groupId);

        // 根据员工工号查找员工ID
        Map<String, Object> userMap = UserContext.getUser();
        String employeeNumber = (String)userMap.get("employeeNumber");
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            Long employeeId = findEmployeeIdByNumber(employeeNumber);
            entity.setEmployeeId(employeeId);
        }

        entity.setDatetime(LocalDateTime.now());
        productStoreMapper.insert(entity);

        // 出库后 actualProduction +1
        production.setActualProduction(actualProduction + 1);
        productProductionMapper.updateById(production);
    }

    @Override
    public PageBean getStoreList(int pageNum, int pageSize, String barCode, Integer action, Integer groupId, String employeeNumber, String deviceSn) {
        QueryWrapper<ProductStore> wrapper = new QueryWrapper<>();

        // 如果传了条形码，则根据barCode查找productId再筛选
        if (barCode != null && !barCode.isBlank()) {
            ProductProduct product = productProductMapper.selectOne(
                    new QueryWrapper<ProductProduct>().eq("bar_code", barCode).eq("delete_flag", 0));
            if (product == null) {
                return buildPageBean(new ArrayList<>(), pageNum, pageSize);
            }
            wrapper.eq("product_id", product.getId());
        }

        wrapper.eq(action != null, "action", action)
                .eq(groupId != null, "group_id", groupId)
                .eq(deviceSn != null && !deviceSn.isBlank(), "device_sn", deviceSn);

        // 如果传了员工工号，则根据工号查找employeeId再筛选
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            Long empId = findEmployeeIdByNumber(employeeNumber);
            wrapper.eq("employee_id", empId);
        }

        wrapper.orderByDesc("id");

        List<ProductStore> stores = productStoreMapper.selectList(wrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (ProductStore store : stores) {
            resultList.add(buildStoreDetail(store));
        }
        return buildPageBean(resultList, pageNum, pageSize);
    }

    private Long findEmployeeIdByNumber(String employeeNumber) {
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.eq("employee_number", employeeNumber);
        Employee employee = employeeMapper.selectOne(wrapper);
        if (employee == null) {
            throw new BusinessException(NOT_FOUND_CODE, "员工不存在");
        }
        return employee.getEmployeeId().longValue();
    }

    private Long findGroupIdByDeviceSn(String deviceSn) {
        QueryWrapper<DeviceGroupDevice> wrapper = new QueryWrapper<>();
        wrapper.eq("device_sn", deviceSn).orderByDesc("id").last("LIMIT 1");
        DeviceGroupDevice device = deviceGroupDeviceMapper.selectOne(wrapper);
        return device != null ? device.getGroupId() : null;
    }

    private Map<String, Object> buildStoreDetail(ProductStore store) {
        ProductProduct product = productProductMapper.selectById(store.getProductId());

        // 获取员工名称
        String employeeName = null;
        Long empId = store.getEmployeeId();
        if (empId != null) {
            Employee employee = employeeMapper.selectById(empId.intValue());
            employeeName = employee != null ? employee.getName() : null;
        }

        // 获取分组名称
        String groupName = null;
        if (store.getGroupId() != null) {
            DeviceGroup group = deviceGroupMapper.selectById(store.getGroupId());
            groupName = group != null ? group.getName() : null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", store.getId());
        result.put("productId", store.getProductId());
        result.put("barCode", product != null ? product.getBarCode() : null);
        result.put("productCode", product != null ? product.getProductCode() : null);
        result.put("deleteFlag", product != null ? product.isDeleteFlag() : null);
        result.put("action", store.getAction());
        result.put("deviceSn", store.getDeviceSn());
        result.put("groupName", groupName);
        result.put("employeeName", employeeName);
        result.put("datetime", store.getDatetime());
        return result;
    }
}
