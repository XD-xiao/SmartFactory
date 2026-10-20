package com.example.smartfactoryservice.Service.DeviceModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface GroupService {

    PageBean getGroupList(Integer id, String name, int pageNum, int pageSize, Integer sort);

    void addGroup(Map<String, Object> group);

    void updateGroup(Integer id, Map<String, Object> group);

    void deleteGroup(String id);
}
