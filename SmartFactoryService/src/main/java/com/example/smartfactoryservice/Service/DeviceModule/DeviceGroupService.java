package com.example.smartfactoryservice.Service.DeviceModule;

import java.util.Map;

public interface DeviceGroupService {

    void joinGroup(Map<String, Object> params);

    void quitGroup(Map<String, Object> params);
}
