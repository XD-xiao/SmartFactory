package com.example.smartfactoryservice.Service.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;

import java.util.Map;

public interface PositionService {

    void addPosition(Map<String, Object> position);

    PageBean getPositionList(Integer positionId, String positionName, int pageNum, int pageSize, Integer sort);

    void deletePosition(Integer positionId);

    void updatePosition(Map<String, Object> position);
}
