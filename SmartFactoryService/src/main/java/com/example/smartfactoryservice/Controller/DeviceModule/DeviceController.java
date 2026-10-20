package com.example.smartfactoryservice.Controller.DeviceModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.DeviceModule.DeviceService;
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
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/getDeviceList")
    public Result<PageBean> getDeviceList(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer state,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer sort
    ) {
        return Result.success("获取设备列表成功",
                deviceService.getDeviceList(id, sn, type, state, pageNum, pageSize, sort));
    }

    @PostMapping("/addDevice")
    public Result<Void> addDevice(@RequestBody(required = false) Map<String, Object> device) {
        deviceService.addDevice(device);
        return Result.success("添加设备成功", null);
    }

    @PutMapping("/updateDevice")
    public Result<Void> updateDevice(
            @RequestParam(required = false) Integer id,
            @RequestBody(required = false) Map<String, Object> device) {
        deviceService.updateDevice(id, device);
        return Result.success("修改设备成功", null);
    }

    @DeleteMapping("/deleteDevice")
    public Result<Void> deleteDevice(@RequestParam(required = false) Integer id) {
        deviceService.deleteDevice(id);
        return Result.success("删除设备成功", null);
    }
}
