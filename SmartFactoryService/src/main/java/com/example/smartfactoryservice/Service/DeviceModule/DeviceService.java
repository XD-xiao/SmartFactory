package com.example.smartfactoryservice.Service.DeviceModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface DeviceService {

    PageBean getDeviceList(Integer id, String sn, String type, Integer state,
                           int pageNum, int pageSize, Integer sort);

    void addDevice(Map<String, Object> device);

    void updateDevice(Integer id, Map<String, Object> device);

    void deleteDevice(Integer id);
}
