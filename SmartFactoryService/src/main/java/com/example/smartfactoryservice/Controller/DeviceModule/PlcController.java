package com.example.smartfactoryservice.Controller.DeviceModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.DeviceModule.PlcService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device")
public class PlcController {

    private final PlcService plcService;

    @GetMapping("/getPlcList")
    public Result<PageBean> getMaintenanceList(
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) Integer seqNo,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize
    ) {

        return Result.success("获取产线运行状态列表成功", plcService.getPlcList(sn, seqNo, pageNum, pageSize));
    }

    @PostMapping("/controlPlc")
    public Result<Void> controlPlc(@RequestBody Map<String, Object> params) {
        String sn = (String) params.get("sn");
        Integer index = (Integer) params.get("index");
        Integer action = (Integer) params.get("action");
        plcService.controlPlc(sn, index, action);
        return Result.success("控制指令已发送", null);
    }

}
