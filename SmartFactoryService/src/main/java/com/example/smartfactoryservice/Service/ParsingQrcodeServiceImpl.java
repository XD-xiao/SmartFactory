package com.example.smartfactoryservice.Service;

import com.example.smartfactoryservice.Exception.BusinessException;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 二维码解析业务服务实现
 * 集成 ZXing 二维码识别库，支持 QR_CODE 格式，
 * 包含图片预处理（灰度化、锐化增强、自适应缩放）以提升识别准确率
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ParsingQrcodeServiceImpl implements ParsingQrcodeService {

    /** 允许上传的图片格式 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "bmp");
    /** 最大文件大小：5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Override
    public String parseQrcode(MultipartFile file) {
        validateFile(file);

        BufferedImage image;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (Exception e) {
            log.error("读取图片文件失败", e);
            throw new BusinessException(400, "无法读取图片文件，请确认文件为有效图片");
        }
        if (image == null) {
            throw new BusinessException(400, "无法解析图片文件，请确认文件为有效图片");
        }

        try {
            // 多流水线尝试策略：按优先级依次尝试不同预处理方式，提高识别成功率
            String result;

            // Pipeline 1: 原始图片直接解码
            result = decodeQrcode(image);
            if (result != null && !result.isEmpty()) {
                log.info("二维码解析成功(原始图片): {}", result);
                return result;
            }

            // Pipeline 2: 灰度化后尝试
            BufferedImage gray = toGrayscale(image);
            result = decodeQrcode(gray);
            if (result != null && !result.isEmpty()) {
                log.info("二维码解析成功(灰度): {}", result);
                return result;
            }

            // Pipeline 3: 锐化增强边缘
            BufferedImage sharpened = sharpen(gray);
            result = decodeQrcode(sharpened);
            if (result != null && !result.isEmpty()) {
                log.info("二维码解析成功(锐化): {}", result);
                return result;
            }

            // Pipeline 4: 多尺度缩放
            double[] scales = {0.5, 0.75, 1.25, 1.5, 2.0, 2.5};
            for (double scale : scales) {
                BufferedImage scaled = scaleImage(gray, scale);
                result = decodeQrcode(scaled);
                if (result != null && !result.isEmpty()) {
                    log.info("二维码解析成功(缩放x{}): {}", scale, result);
                    return result;
                }
            }

            // Pipeline 5: 二值化
            BufferedImage binary = binarize(gray);
            result = decodeQrcode(binary);
            if (result != null && !result.isEmpty()) {
                log.info("二维码解析成功(二值化): {}", result);
                return result;
            }

            throw new BusinessException(400, "未能识别到二维码，请上传清晰的二维码图片");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("二维码解析异常", e);
            throw new BusinessException(500, "二维码解析失败：" + e.getMessage());
        }
    }

    /**
     * 校验上传文件的格式和大小
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "文件大小超过上限，最大允许 5MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(400, "文件名为空");
        }
        String extension = getFileExtension(originalFilename);
        if (extension.isEmpty() || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(400, "不支持的文件格式（" + extension + "），仅允许 png、jpg、jpeg、bmp 格式");
        }
    }

    /**
     * 获取文件扩展名（不含点号）
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1);
    }

    /**
     * 拉普拉斯锐化增强，强化二维码模块边界特征
     */
    private BufferedImage sharpen(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage gray = ensureGrayType(source);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        float[] laplacian = {
            0, -1,  0,
           -1,  5, -1,
            0, -1,  0
        };
        Kernel kernel = new Kernel(3, 3, laplacian);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        op.filter(gray, result);
        return result;
    }

    /**
     * 自适应缩放图片
     */
    private BufferedImage scaleImage(BufferedImage source, double scale) {
        int newWidth = (int) Math.round(source.getWidth() * scale);
        int newHeight = (int) Math.round(source.getHeight() * scale);
        if (newWidth < 10 || newHeight < 10) {
            return source;
        }
        BufferedImage graySource = ensureGrayType(source);
        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(graySource, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return scaled;
    }

    /**
     * 确保图片为 TYPE_BYTE_GRAY 格式
     */
    private BufferedImage ensureGrayType(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return source;
        }
        BufferedImage gray = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return gray;
    }

    /**
     * 将图片转换为灰度图
     */
    private BufferedImage toGrayscale(BufferedImage source) {
        BufferedImage gray = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return gray;
    }

    /**
     * 使用 Otsu 算法进行二值化
     */
    private BufferedImage binarize(BufferedImage gray) {
        int width = gray.getWidth();
        int height = gray.getHeight();

        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = gray.getRaster().getSample(x, y, 0);
                histogram[pixel]++;
            }
        }

        int total = width * height;
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;
        double maxVariance = 0;
        int threshold = 128;

        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0) continue;
            int wF = total - wB;
            if (wF == 0) break;
            sumB += (double) t * histogram[t];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double variance = (double) wB * (double) wF * (mB - mF) * (mB - mF);
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = t;
            }
        }

        BufferedImage binary = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = gray.getRaster().getSample(x, y, 0);
                int value = pixel > threshold ? 255 : 0;
                binary.getRaster().setSample(x, y, 0, value);
            }
        }
        return binary;
    }

    /**
     * 使用 ZXing MultiFormatReader 解码二维码
     */
    private String decodeQrcode(BufferedImage image) {
        if (image.getWidth() < 10 || image.getHeight() < 10) {
            return null;
        }

        String result = tryDecode(image);
        if (result != null) return result;

        return null;
    }

    /**
     * 单次解码尝试
     */
    private String tryDecode(BufferedImage image) {
        try {
            BufferedImage decodedImage = image;
            if (image.getType() == BufferedImage.TYPE_BYTE_BINARY) {
                decodedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = decodedImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
            }

            LuminanceSource source = new BufferedImageLuminanceSource(decodedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, java.util.List.of(
                    BarcodeFormat.QR_CODE
            ));

            MultiFormatReader reader = new MultiFormatReader();
            reader.setHints(hints);
            Result result = reader.decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            log.debug("解码尝试失败: {}", e.getMessage());
            return null;
        }
    }
}
