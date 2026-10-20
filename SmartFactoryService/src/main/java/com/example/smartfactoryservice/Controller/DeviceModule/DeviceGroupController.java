package com.example.smartfactoryservice.Controller.DeviceModule;

import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.DeviceModule.DeviceGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device")
public class DeviceGroupController {

    private final DeviceGroupService deviceGroupService;

    @PostMapping("/joinGroup")
    public Result<Void> joinGroup(@RequestBody(required = false) Map<String, Object> params) {
        deviceGroupService.joinGroup(params);
        return Result.success("设备加入分组成功", null);
    }

    @DeleteMapping("/quitGroup")
    public Result<Void> quitGroup(@RequestBody(required = false) Map<String, Object> params) {
        deviceGroupService.quitGroup(params);
        return Result.success("设备退出分组成功", null);
    }
}
