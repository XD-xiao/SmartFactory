package com.example.smartfactoryservice.Controller.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.ProductModule.ProductProductionService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/product")
public class ProductProductionController {

    private final ProductProductionService productProductionService;

    @PostMapping("/setProduction")
    public Result<Void> setProduction(@RequestBody(required = false) Map<String, Object> production) {
        productProductionService.setProduction(production);
        return Result.success("设置产品产量成功", null);
    }

    @PutMapping("/updateProduction")
    public Result<Void> updateProduction(@RequestBody(required = false) Map<String, Object> production) {
        productProductionService.updateProduction(production);
        return Result.success("更新产品产量成功", null);
    }

    @GetMapping("/getProductionList")
    public Result<PageBean> getProductionList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) String barCode,
            @RequestParam(required = false) String planName
    ) {
        return Result.success("获取产品产量列表成功",
                productProductionService.getProductionList(pageNum, pageSize, barCode, planName));
    }

    @PostMapping("/qualityCheck")
    public Result<Void> qualityCheck(@RequestBody(required = false) Map<String, Object> params) {
        productProductionService.qualityCheck(params);
        return Result.success("质检通过", null);
    }
}
