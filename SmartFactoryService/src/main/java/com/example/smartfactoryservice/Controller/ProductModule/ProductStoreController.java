package com.example.smartfactoryservice.Controller.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.ProductModule.ProductStoreService;
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
@RequestMapping("/api/product")
public class ProductStoreController {

    private final ProductStoreService productStoreService;

    @PostMapping("/storeIn")
    public Result<Void> storeIn(@RequestBody Map<String, Object> storeInfo) {
        productStoreService.storeIn(storeInfo);
        return Result.success("产品入库成功", null);
    }

    @PostMapping("/storeOut")
    public Result<Void> storeOut(@RequestBody Map<String, Object> storeInfo) {
        productStoreService.storeOut(storeInfo);
        return Result.success("产品出库成功", null);
    }

    @GetMapping("/getStoreList")
    public Result<PageBean> getStoreList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) String barCode,
            @RequestParam(required = false) Integer action,
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) String employeeNumber,
            @RequestParam(required = false) String deviceSn
    ) {
        return Result.success("获取出入库记录列表成功",
                productStoreService.getStoreList(pageNum, pageSize, barCode, action, groupId, employeeNumber, deviceSn));
    }
}
