package com.example.smartfactoryservice.Controller.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.ProductModule.ProductService;
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
public class ProductController {

    private final ProductService productService;

    @PostMapping("/addProduct")
    public Result<Void> addProduct(@RequestBody(required = false) Map<String, Object> product) {
        productService.addProduct(product);
        return Result.success("添加产品成功", null);
    }

    @GetMapping("/getProductList")
    public Result<PageBean> getProductList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) String barCode,
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String deviceSn
    ) {
        return Result.success("获取产品列表成功",
                productService.getProductList(pageNum, pageSize, barCode, typeCode, modelCode, productCode, deviceSn));
    }

    @GetMapping("/getProduct")
    public Result<Object> getProduct(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String barCode
    ) {
        return Result.success("获取产品详情成功", productService.getProduct(id, barCode));
    }

    @PutMapping("/updateProduct")
    public Result<Void> updateProduct(@RequestBody(required = false) Map<String, Object> product) {
        productService.updateProduct(product);
        return Result.success("修改产品成功", null);
    }

    @DeleteMapping("/deleteProduct")
    public Result<Void> deleteProduct(@RequestParam(required = false) Integer id) {
        productService.deleteProduct(id);
        return Result.success("删除产品成功", null);
    }
}
