package com.example.smartfactoryservice.Service.DeviceModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceMachineFailure;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceMachineFailureMapper;
import com.example.smartfactoryservice.Service.DeviceModule.MaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MaintenanceServiceImpl extends DeviceServiceSupport implements MaintenanceService {

    private final DeviceMachineFailureMapper deviceMachineFailureMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMaintenance(Map<String, Object> maintenance) {
        requireBody(maintenance, "维修申请信息不能为空");

        DeviceMachineFailure entity = new DeviceMachineFailure();
        entity.setSn(requireString(maintenance, "设备SN", "sn"));
        entity.setReason(requireString(maintenance, "故障原因", "reason"));
        String failureLevel = requireString(maintenance, "故障级别", "failureLevel");
        entity.setFailureLevel(toInteger(failureLevel));
        entity.setFailureTime(requireString(maintenance, "故障时间", "failureTime"));
        entity.setStatus(0);

        deviceMachineFailureMapper.insert(entity);
    }

    @Override
    public PageBean getMaintenanceList(String sn, String reason, Integer failureLevel,
                                       int pageNum, int pageSize, Integer status) {
        QueryWrapper<DeviceMachineFailure> wrapper = new QueryWrapper<>();
        wrapper.eq(sn != null && !sn.isBlank(), "sn", sn)
                .like(reason != null && !reason.isBlank(), "reason", reason)
                .eq(failureLevel != null, "failure_level", failureLevel)
                .eq(status != null, "status", status)
                .orderByDesc("id");

        List<DeviceMachineFailure> list = deviceMachineFailureMapper.selectList(wrapper);
        return buildPageBean(list, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMaintenance(Integer id, Map<String, Object> maintenance) {
        requireBody(maintenance, "维修申请信息不能为空");
        if (id == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "维修申请ID不能为空");
        }

        DeviceMachineFailure entity = deviceMachineFailureMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "维修申请不存在");
        }

        Integer status = requireInteger(maintenance, "状态", "status");
        entity.setStatus(status);
        deviceMachineFailureMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMaintenance(Integer id) {
        if (id == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "维修申请ID不能为空");
        }

        if (deviceMachineFailureMapper.deleteById(id) == 0) {
            throw new BusinessException(NOT_FOUND_CODE, "维修申请不存在");
        }
    }
}
