package com.example.smartfactoryservice.Controller;

import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.ParsingBarcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 条形码解析控制器
 * 接收图片上传请求，调用条形码解析服务返回识别结果
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/barcode")
public class ParsingBarcodecontroller {

    private final ParsingBarcodeService parsingBarcodeService;

    /**
     * 上传条形码图片进行解析
     *
     * @param file 条形码图片文件（支持 png、jpg、jpeg、bmp 格式，最大 5MB）
     * @return 统一格式 JSON 响应，data 为解析出的条形码字符串
     */
    @PostMapping("/parseBarcode")
    public Result<String> parseBarcode(@RequestParam("file") MultipartFile file) {
        String barcode = parsingBarcodeService.parseBarcode(file);
        return Result.success("条形码解析成功", barcode);
    }
}
