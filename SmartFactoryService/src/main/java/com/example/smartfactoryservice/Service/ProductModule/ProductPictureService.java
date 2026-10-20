package com.example.smartfactoryservice.Service.ProductModule;

import java.util.Map;

public interface ProductPictureService {

    Map<String, Object> uploadPicture(Map<String, Object> picture);

    Map<String, Object> getPicture(Integer id);
}
