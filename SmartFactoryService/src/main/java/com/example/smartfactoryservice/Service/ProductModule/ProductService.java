package com.example.smartfactoryservice.Service.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface ProductService {

    void addProduct(Map<String, Object> product);

    PageBean getProductList(int pageNum, int pageSize, String barCode, String typeCode, String modelCode, String productCode, String deviceSn);

    Map<String, Object> getProduct(Integer id, String barCode);

    void updateProduct(Map<String, Object> product);

    void deleteProduct(Integer id);
}
