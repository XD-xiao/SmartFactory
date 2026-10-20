package com.example.smartfactoryservice.Controller;

import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.ParsingQrcodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 二维码解析控制器
 * 接收图片上传请求，调用二维码解析服务返回识别结果
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qrcode")
public class ParsingQrcodeController {

    private final ParsingQrcodeService parsingQrcodeService;

    /**
     * 上传二维码图片进行解析
     *
     * @param file 二维码图片文件（支持 png、jpg、jpeg、bmp 格式，最大 5MB）
     * @return 统一格式 JSON 响应，data 为解析出的二维码字符串
     */
    @PostMapping("/parseQrcode")
    public Result<String> parseQrcode(@RequestParam("file") MultipartFile file) {
        String qrcode = parsingQrcodeService.parseQrcode(file);
        return Result.success("二维码解析成功", qrcode);
    }
}
