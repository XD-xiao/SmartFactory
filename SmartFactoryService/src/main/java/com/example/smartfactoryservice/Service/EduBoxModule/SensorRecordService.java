package com.example.smartfactoryservice.Service.EduBoxModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

/**
 * 传感器记录查询服务。
 */
public interface SensorRecordService {

    /**
     * 分页查询传感器记录，按传感器配置ID和状态筛选。
     */
    PageBean pageList(Long id, Integer status, int pageNum, int pageSize);
}
