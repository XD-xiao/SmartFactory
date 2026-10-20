package com.example.smartfactoryservice.Controller.DeviceModule;

import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorConfig;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.EduBoxModule.SensorConfigService;
import com.example.smartfactoryservice.Service.EduBoxModule.SensorRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 试验箱 & 传感器管理控制器。
 * 试验箱列表、传感器配置/记录通过 {@link SensorConfigService} 处理，
 * 传感器类型由 sensorType 编号（如 "25"）指定，按不同传感器分发到对应的业务方法。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/eduBox")
public class EduBoxController {

    private final SensorConfigService sensorConfigService;
    private final SensorRecordService sensorRecordService;

    /**
     * 根据 ID 获取单条传感器配置。
     */
    @GetMapping("/getSensorConfig")
    public Result<SensorConfig> getSensorConfig(@RequestParam Long id) {
        return Result.success("获取传感器配置成功", sensorConfigService.getById(id));
    }

    /**
     * 分页查询传感器配置列表。
     */
    @GetMapping("/getSensorConfigList")
    public Result<PageBean> getSensorConfigList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) Long sceneType,
            @RequestParam(required = false) Long sensor) {
        return Result.success("获取传感器配置列表成功",
                sensorConfigService.pageList(pageNum, pageSize, sn, sceneType, sensor));
    }

    /**
     * 新增传感器配置。
     */
    @PostMapping("/addSensorConfig")
    public Result<Void> addSensorConfig(@RequestBody(required = false) SensorConfig config) {
        sensorConfigService.save(config);
        return Result.success("添加传感器配置成功", null);
    }

    /**
     * 更新传感器配置。
     */
    @PutMapping("/updateSensorConfig")
    public Result<Void> updateSensorConfig(@RequestBody(required = false) SensorConfig config) {
        sensorConfigService.update(config);
        return Result.success("更新传感器配置成功", null);
    }

    /**
     * 删除传感器配置（软删除）。
     */
    @DeleteMapping("/deleteSensorConfig")
    public Result<Void> deleteSensorConfig(@RequestParam Long id) {
        sensorConfigService.delete(id);
        return Result.success("删除传感器配置成功", null);
    }

    /**
     * 分页查询传感器记录列表。
     */
    @GetMapping("/getSensorRecordList")
    public Result<PageBean> getSensorRecordList(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        return Result.success("获取传感器记录列表成功",
                sensorRecordService.pageList(id, status, pageNum, pageSize));
    }


}
