package com.example.smartfactoryservice.Service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 条形码解析业务服务接口
 */
public interface ParsingBarcodeService {

    /**
     * 解析条形码图片，返回识别到的条形码字符串
     *
     * @param file 上传的图片文件（MultipartFile）
     * @return 解析得到的条形码字符串
     * @throws com.example.smartfactoryservice.Exception.BusinessException 文件格式不符、文件过大或无法识别时抛出
     */
    String parseBarcode(MultipartFile file);
}
