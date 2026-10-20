package com.example.smartfactoryservice.Service.DeviceModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroup;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceGroupDevice;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupDeviceMapper;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceGroupMapper;
import com.example.smartfactoryservice.Service.DeviceModule.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends DeviceServiceSupport implements GroupService {

    private final DeviceGroupMapper deviceGroupMapper;
    private final DeviceGroupDeviceMapper deviceGroupDeviceMapper;

    @Override
    public PageBean getGroupList(Integer id, String name, int pageNum, int pageSize, Integer sort) {
        QueryWrapper<DeviceGroup> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null, "id", id)
                .like(name != null && !name.isBlank(), "name", name);

        if (sort != null && sort == 1) {
            wrapper.orderByAsc("id");
        } else {
            wrapper.orderByDesc("id");
        }

        List<DeviceGroup> groups = deviceGroupMapper.selectList(wrapper);
        return buildPageBean(groups, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGroup(Map<String, Object> group) {
        requireBody(group, "分组信息不能为空");

        DeviceGroup entity = new DeviceGroup();
        entity.setName(requireString(group, "分组名称", "name"));
        deviceGroupMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(Integer id, Map<String, Object> group) {
        requireBody(group, "分组信息不能为空");
        if (id == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "分组ID不能为空");
        }

        DeviceGroup entity = deviceGroupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "分组不存在");
        }

        String name = getString(group, "name");
        if (name != null) {
            entity.setName(name);
        }
        deviceGroupMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(String id) {
        Long groupId = toLong(id);
        if (groupId == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "分组ID不能为空");
        }

        QueryWrapper<DeviceGroupDevice> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        long count = deviceGroupDeviceMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "当前分组下还有 " + count + " 个设备，无法删除");
        }

        if (deviceGroupMapper.deleteById(groupId) == 0) {
            throw new BusinessException(NOT_FOUND_CODE, "分组不存在");
        }
    }
}
