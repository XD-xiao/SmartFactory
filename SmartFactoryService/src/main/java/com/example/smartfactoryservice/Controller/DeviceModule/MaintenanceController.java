package com.example.smartfactoryservice.Controller.DeviceModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.DeviceModule.MaintenanceService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping("/addMaintenance")
    public Result<Void> addMaintenance(@RequestBody(required = false) Map<String, Object> maintenance) {
        maintenanceService.addMaintenance(maintenance);
        return Result.success("添加维修申请成功", null);
    }

    @GetMapping("/getMaintenanceList")
    public Result<PageBean> getMaintenanceList(
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) Integer failureLevel,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success("获取维修申请列表成功",
                maintenanceService.getMaintenanceList(sn, reason, failureLevel, pageNum, pageSize, status));
    }

    @PutMapping("/updateMaintenance")
    public Result<Void> updateMaintenance(
            @RequestParam(required = false) Integer id,
            @RequestBody(required = false) Map<String, Object> maintenance) {
        maintenanceService.updateMaintenance(id, maintenance);
        return Result.success("修改维修申请成功", null);
    }

    @DeleteMapping("/deleteMaintenance")
    public Result<Void> deleteMaintenance(@RequestParam(required = false) Integer id) {
        maintenanceService.deleteMaintenance(id);
        return Result.success("删除设备故障成功", null);
    }
}
