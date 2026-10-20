package com.example.smartfactoryservice.Service.DeviceModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceDevice;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroup;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroupDevice;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceDeviceMapper;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupDeviceMapper;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupMapper;
import com.example.smartfactoryservice.Service.DeviceModule.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends DeviceServiceSupport implements DeviceService {

    private final DeviceDeviceMapper deviceDeviceMapper;
    private final DeviceGroupDeviceMapper deviceGroupDeviceMapper;
    private final DeviceGroupMapper deviceGroupMapper;

    @Override
    public PageBean getDeviceList(Integer id, String sn, String type, Integer state,
                                  int pageNum, int pageSize, Integer sort) {
        QueryWrapper<DeviceDevice> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id)
                .like(sn != null && !sn.isBlank(), "sn", sn)
                .like(type != null && !type.isBlank(), "type", type)
                .eq(state != null, "state", state);

        if (sort != null && sort == 1) {
            wrapper.orderByAsc("id");
        } else {
            wrapper.orderByDesc("id");
        }

        List<DeviceDevice> devices = deviceDeviceMapper.selectList(wrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();

        if (!devices.isEmpty()) {
            List<String> snList = devices.stream()
                    .map(DeviceDevice::getSn)
                    .collect(Collectors.toList());

            QueryWrapper<DeviceGroupDevice> gdWrapper = new QueryWrapper<>();
            gdWrapper.in("device_sn", snList);
            List<DeviceGroupDevice> groupDevices = deviceGroupDeviceMapper.selectList(gdWrapper);

            Map<String, DeviceGroupDevice> snToGroupDevice = groupDevices.stream()
                    .collect(Collectors.toMap(DeviceGroupDevice::getDeviceSn, gd -> gd, (a, b) -> a));

            Map<Long, String> groupIdToName = new HashMap<>();
            if (!groupDevices.isEmpty()) {
                List<Long> groupIds = groupDevices.stream()
                        .map(DeviceGroupDevice::getGroupId)
                        .distinct()
                        .collect(Collectors.toList());
                List<DeviceGroup> groups = deviceGroupMapper.selectBatchIds(groupIds);
                for (DeviceGroup group : groups) {
                    groupIdToName.put(group.getId(), group.getName());
                }
            }

            for (DeviceDevice device : devices) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", device.getId());
                item.put("sn", device.getSn());
                item.put("type", device.getType());
                item.put("state", device.getState());
                item.put("lastOnlineTime", device.getLastOnlineTime());
                item.put("createTime", device.getCreateTime());
                item.put("updateTime", device.getUpdateTime());

                DeviceGroupDevice gd = snToGroupDevice.get(device.getSn());
                if (gd != null) {
                    item.put("groupId", gd.getGroupId());
                    item.put("groupName", groupIdToName.getOrDefault(gd.getGroupId(), ""));
                } else {
                    item.put("groupId", null);
                    item.put("groupName", null);
                }

                resultList.add(item);
            }
        }

        return buildPageBean(resultList, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDevice(Map<String, Object> device) {
        requireBody(device, "设备信息不能为空");

        String sn = requireString(device, "设备SN", "sn");
        String type = requireString(device, "设备类型", "type");

        QueryWrapper<DeviceDevice> wrapper = new QueryWrapper<>();
        wrapper.eq("sn", sn);
        if (deviceDeviceMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "设备SN已存在: " + sn);
        }

        DeviceDevice entity = new DeviceDevice();
        entity.setSn(sn);
        entity.setType(type);
        entity.setState(0);
        deviceDeviceMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(Integer id, Map<String, Object> device) {
        requireBody(device, "设备信息不能为空");
        if (id == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "设备ID不能为空");
        }

        DeviceDevice entity = deviceDeviceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "设备不存在");
        }

        String sn = requireString(device, "设备SN", "sn");
        String type = requireString(device, "设备类型", "type");
        Integer state = getInteger(device, "state");

        if (!sn.equals(entity.getSn())) {
            QueryWrapper<DeviceDevice> wrapper = new QueryWrapper<>();
            wrapper.eq("sn", sn);
            if (deviceDeviceMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(BAD_REQUEST_CODE, "设备SN已存在: " + sn);
            }
        }

        entity.setSn(sn);
        entity.setType(type);
        if (state != null) {
            entity.setState(state);
        }
        deviceDeviceMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Integer id) {
        if (id == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "设备ID不能为空");
        }

        DeviceDevice entity = deviceDeviceMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "设备不存在");
        }

        QueryWrapper<DeviceGroupDevice> wrapper = new QueryWrapper<>();
        wrapper.eq("device_sn", entity.getSn());
        deviceGroupDeviceMapper.delete(wrapper);

        deviceDeviceMapper.deleteById(id);
    }
}
