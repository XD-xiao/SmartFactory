package com.example.smartfactoryservice.Controller.ProductModule;

import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.ProductModule.ProductPictureService;
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
public class ProductPictureController {

    private final ProductPictureService productPictureService;

    @PostMapping("/uploadPicture")
    public Result<Object> uploadPicture(@RequestBody(required = false) Map<String, Object> picture) {
        return Result.success("上传产品图片成功", productPictureService.uploadPicture(picture));
    }

    @GetMapping("/getPicture")
    public Result<Object> getPicture(@RequestParam(required = false) Integer id) {
        return Result.success("获取产品图片成功", productPictureService.getPicture(id));
    }
}
