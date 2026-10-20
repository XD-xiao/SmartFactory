package com.example.smartfactoryservice.Service.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface ProductStoreService {

    void storeIn(Map<String, Object> storeInfo);

    void storeOut(Map<String, Object> storeInfo);

    PageBean getStoreList(int pageNum, int pageSize, String barCode, Integer action, Integer groupId, String employeeNumber, String deviceSn);
}
