package com.example.smartfactoryservice.Controller.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.ProductModule.ProductTypeService;
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
public class ProductTypeController {

    private final ProductTypeService productTypeService;

    @PostMapping("/addType")
    public Result<Void> addType(@RequestBody(required = false) Map<String, Object> type) {
        productTypeService.addType(type);
        return Result.success("添加产品类型成功", null);
    }

    @GetMapping("/getTypeList")
    public Result<PageBean> getTypeList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code
    ) {
        return Result.success("获取产品类型列表成功",
                productTypeService.getTypeList(pageNum, pageSize, name, code));
    }

    @PutMapping("/updateType")
    public Result<Void> updateType(@RequestBody(required = false) Map<String, Object> type) {
        productTypeService.updateType(type);
        return Result.success("修改产品类型成功", null);
    }

    @DeleteMapping("/deleteType")
    public Result<Void> deleteType(@RequestParam(required = false) Integer id) {
        productTypeService.deleteType(id);
        return Result.success("删除产品类型成功", null);
    }
}
