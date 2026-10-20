package com.example.smartfactoryservice.Service.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface ProductTypeService {

    void addType(Map<String, Object> type);

    PageBean getTypeList(int pageNum, int pageSize, String name, String code);

    void updateType(Map<String, Object> type);

    void deleteType(Integer id);
}
