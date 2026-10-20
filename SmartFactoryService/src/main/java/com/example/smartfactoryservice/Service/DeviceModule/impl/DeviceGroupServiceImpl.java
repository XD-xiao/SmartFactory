package com.example.smartfactoryservice.Service.DeviceModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroup;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroupDevice;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupDeviceMapper;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupMapper;
import com.example.smartfactoryservice.Service.DeviceModule.DeviceGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeviceGroupServiceImpl extends DeviceServiceSupport implements DeviceGroupService {

    private final DeviceGroupDeviceMapper deviceGroupDeviceMapper;
    private final DeviceGroupMapper deviceGroupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinGroup(Map<String, Object> params) {
        requireBody(params, "参数不能为空");

        Long groupId = requireLong(params, "分组ID", "groupId");
        String deviceSn = requireString(params, "设备SN", "deviceSn");

        DeviceGroup group = deviceGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(NOT_FOUND_CODE, "分组不存在");
        }

        QueryWrapper<DeviceGroupDevice> wrapper = new QueryWrapper<>();
        wrapper.eq("device_sn", deviceSn);
        if (deviceGroupDeviceMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "设备 " + deviceSn + " 已加入其他分组，一个设备只能加入一个分组");
        }

        DeviceGroupDevice entity = new DeviceGroupDevice();
        entity.setGroupId(groupId);
        entity.setDeviceSn(deviceSn);
        deviceGroupDeviceMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Map<String, Object> params) {
        requireBody(params, "参数不能为空");

        String deviceSn = requireString(params, "设备SN", "deviceSn");

        QueryWrapper<DeviceGroupDevice> wrapper = new QueryWrapper<>();
        wrapper.eq("device_sn", deviceSn);

        if (deviceGroupDeviceMapper.delete(wrapper) == 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "设备 " + deviceSn + " 不在任何分组中");
        }
    }
}
