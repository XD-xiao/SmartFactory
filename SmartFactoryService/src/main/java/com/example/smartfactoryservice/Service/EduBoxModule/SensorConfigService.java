package com.example.smartfactoryservice.Service.EduBoxModule;

import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorConfig;
import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

/**
 * 传感器配置与记录管理服务。
 * 提供每种传感器的配置获取、阈值修改、配置删除、记录分页查询等操作。
 */
public interface SensorConfigService {

    /**
     * 根据主键 ID 查询传感器配置。
     */
    SensorConfig getById(Long id);

    /**
     * 分页查询传感器配置，可按 sn、场景类型、传感器编号筛选。
     */
    PageBean pageList(int pageNum, int pageSize, String sn, Long sceneType, Long sensor);

    /**
     * 新增一条传感器配置。
     */
    void save(SensorConfig config);

    /**
     * 根据主键 ID 更新传感器配置（全量更新）。
     */
    void update(SensorConfig config);

    /**
     * 软删除传感器配置（设置 delete_flag = true）。
     */
    void delete(Long id);

    /**
     * 接收 MQTT 上报的阈值配置，按传感器编号更新配置表中的阈值字段。
     */
    void updateThresholdFromMqtt(Integer sensor, Map<String, Object> config);
}
