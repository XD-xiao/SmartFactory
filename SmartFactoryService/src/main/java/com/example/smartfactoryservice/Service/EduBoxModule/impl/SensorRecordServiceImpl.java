package com.example.smartfactoryservice.Service.EduBoxModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorRecord;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Mapper.EduBoxModule.SensorRecordMapper;
import com.example.smartfactoryservice.Service.EduBoxModule.SensorRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 传感器记录查询服务实现。
 * 采用"先查全量 + 内存分页"方式，避免依赖 MyBatis-Plus 分页插件。
 */
@Service
@RequiredArgsConstructor
public class SensorRecordServiceImpl implements SensorRecordService {

    private final SensorRecordMapper sensorRecordMapper;

    @Override
    public PageBean pageList(Long id, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<SensorRecord> wrapper = new LambdaQueryWrapper<>();
        if (id != null) {
            wrapper.eq(SensorRecord::getSensorId, id);
        }
        if (status != null) {
            wrapper.eq(SensorRecord::getStatus, status);
        }
        wrapper.orderByDesc(SensorRecord::getDatetime);

        List<SensorRecord> fullList = sensorRecordMapper.selectList(wrapper);

        return buildPageBean(fullList, pageNum, pageSize);
    }

    /**
     * 对全量列表进行内存分页，与 DeviceServiceSupport#buildPageBean 逻辑一致。
     */
    private PageBean buildPageBean(List<?> fullList, int pageNum, int pageSize) {
        List<?> safeList = fullList == null ? Collections.emptyList() : fullList;
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int fromIndex = (safePageNum - 1) * safePageSize;
        if (fromIndex >= safeList.size()) {
            return new PageBean((long) safeList.size(), Collections.emptyList());
        }
        int toIndex = Math.min(fromIndex + safePageSize, safeList.size());
        return new PageBean((long) safeList.size(), safeList.subList(fromIndex, toIndex));
    }
}
