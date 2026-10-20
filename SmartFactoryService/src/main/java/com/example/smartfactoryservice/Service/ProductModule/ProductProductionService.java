package com.example.smartfactoryservice.Service.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface ProductProductionService {

    void setProduction(Map<String, Object> production);

    void updateProduction(Map<String, Object> production);

    PageBean getProductionList(int pageNum, int pageSize, String barCode, String planName);

    void qualityCheck(Map<String, Object> params);
}
