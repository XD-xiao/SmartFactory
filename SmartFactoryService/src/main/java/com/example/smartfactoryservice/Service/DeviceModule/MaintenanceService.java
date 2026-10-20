package com.example.smartfactoryservice.Service.DeviceModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface MaintenanceService {

    void addMaintenance(Map<String, Object> maintenance);

    PageBean getMaintenanceList(String sn, String reason, Integer failureLevel,
                                int pageNum, int pageSize, Integer status);

    void updateMaintenance(Integer id, Map<String, Object> maintenance);

    void deleteMaintenance(Integer id);
}
