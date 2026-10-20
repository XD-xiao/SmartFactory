package com.example.smartfactoryservice.Controller.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.ProductModule.ProductModelService;
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
@RequestMapping("/api/product")
public class ProductModelController {

    private final ProductModelService productModelService;

    @PostMapping("/addModel")
    public Result<Void> addModel(@RequestBody(required = false) Map<String, Object> model) {
        productModelService.addModel(model);
        return Result.success("添加产品型号成功", null);
    }

    @GetMapping("/getModelList")
    public Result<PageBean> getModelList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code
    ) {
        return Result.success("获取产品型号列表成功",
                productModelService.getModelList(pageNum, pageSize, name, code));
    }

    @PutMapping("/updateModel")
    public Result<Void> updateModel(@RequestBody(required = false) Map<String, Object> model) {
        productModelService.updateModel(model);
        return Result.success("修改产品型号成功", null);
    }

    @DeleteMapping("/deleteModel")
    public Result<Void> deleteModel(@RequestParam(required = false) Integer id) {
        productModelService.deleteModel(id);
        return Result.success("删除产品型号成功", null);
    }
}
