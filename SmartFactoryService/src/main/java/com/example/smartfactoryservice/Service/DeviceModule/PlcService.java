package com.example.smartfactoryservice.Service.DeviceModule;

import com.example.smartfactoryservice.Domain.Mqtt.PlcSwitchMqttPayload;
import com.example.smartfactoryservice.Domain.Mqtt.PlcSyncMqttPayload;
import com.example.smartfactoryservice.Domain.Vo.PageBean;

public interface PlcService {

    void saveRunTime(PlcSwitchMqttPayload payload);

    void saveSyncRunTimes(PlcSyncMqttPayload payload);

    PageBean getPlcList(String sn, Integer seqNo, int pageNum, int pageSize);

    void controlPlc(String sn, Integer index, Integer action);
}
