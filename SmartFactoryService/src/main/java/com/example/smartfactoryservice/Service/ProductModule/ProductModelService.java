package com.example.smartfactoryservice.Service.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface ProductModelService {

    void addModel(Map<String, Object> model);

    PageBean getModelList(int pageNum, int pageSize, String name, String code);

    void updateModel(Map<String, Object> model);

    void deleteModel(Integer id);
}
