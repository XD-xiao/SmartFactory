package com.example.smartfactoryservice.Service.PersonManagementModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Position;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.EmployeeMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.PositionMapper;
import com.example.smartfactoryservice.Service.PersonManagementModule.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl extends PersonManagementServiceSupport implements PositionService {

    private final PositionMapper positionMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPosition(Map<String, Object> position) {
        requireBody(position, "职级信息不能为空");

        Position entity = new Position();
        entity.setPositionName(requireString(position, "职级名称", "positionName"));
        entity.setRemark(getString(position, "remark"));
        positionMapper.insert(entity);
    }

    @Override
    public PageBean getPositionList(Integer positionId, String positionName, int pageNum, int pageSize, Integer sort) {
        QueryWrapper<Position> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0)
                .eq(positionId != null, "position_id", positionId)
                .like(positionName != null && !positionName.isBlank(), "position_name", positionName);

        if (sort != null && sort == 1) {
            wrapper.orderByAsc("position_id");
        } else {
            wrapper.orderByDesc("position_id");
        }

        List<Position> positions = positionMapper.selectList(wrapper);
        return buildPageBean(positions, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePosition(Integer positionId) {
        if (positionId == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "职级ID不能为空");
        }
        Position entity = positionMapper.selectById(positionId);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "职级不存在");
        }
        // 查询职级下是否有未删除的员工关联
        QueryWrapper<com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee> wrapper = new QueryWrapper<>();
        wrapper.eq("position_id", positionId)
                .eq("delete_flag", 0);
        long count = employeeMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(BAD_REQUEST_CODE, "该职级下还有 " + count + " 名员工，无法删除");
        }
        entity.setDeleteFlag(true);
        positionMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePosition(Map<String, Object> position) {
        requireBody(position, "职级信息不能为空");

        Integer positionId = requireInteger(position, "职级ID", "positionId");
        QueryWrapper<Position> wrapper = new QueryWrapper<>();
        wrapper.eq("position_id", positionId)
                .eq("delete_flag", 0);
        Position entity = positionMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException(NOT_FOUND_CODE, "职级不存在");
        }

        entity.setPositionName(requireString(position, "职级名称", "positionName"));
        entity.setRemark(getString(position, "remark"));
        positionMapper.updateById(entity);
    }
}
