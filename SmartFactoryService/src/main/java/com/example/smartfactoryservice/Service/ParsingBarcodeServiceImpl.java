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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 条形码解析业务服务实现
 * 集成 ZXing 条形码识别库，支持 CODE128 等主流条形码格式，
 * 包含图片预处理（灰度化、二值化、倾斜校正、锐化增强、自适应缩放）以提升识别准确率
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ParsingBarcodeServiceImpl implements ParsingBarcodeService {

    /** 允许上传的图片格式 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "bmp");
    /** 最大文件大小：5MB */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    /** 倾斜校正探测角度范围（度） */
    private static final double DESKEW_ANGLE_RANGE = 30.0;
    /** 倾斜校正角度步长（度） */
    private static final double DESKEW_ANGLE_STEP = 0.5;

    @Override
    public String parseBarcode(MultipartFile file) {
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
            result = decodeBarcode(image);
            if (result != null && !result.isEmpty()) {
                log.info("条形码解析成功(原始图片): {}", result);
                return result;
            }

            // Pipeline 2: 灰度化后尝试
            BufferedImage gray = toGrayscale(image);
            result = decodeBarcode(gray);
            if (result != null && !result.isEmpty()) {
                log.info("条形码解析成功(灰度): {}", result);
                return result;
            }

            // Pipeline 3: 锐化增强边缘，强化条空边界特征
            BufferedImage sharpened = sharpen(gray);
            result = decodeBarcode(sharpened);
            if (result != null && !result.isEmpty()) {
                log.info("条形码解析成功(锐化): {}", result);
                return result;
            }

            // Pipeline 4: 多尺度缩放，兼容不同尺寸和分辨率的条码
            double[] scales = {0.5, 0.75, 1.25, 1.5, 2.0, 2.5};
            for (double scale : scales) {
                BufferedImage scaled = scaleImage(gray, scale);
                result = decodeBarcode(scaled);
                if (result != null && !result.isEmpty()) {
                    log.info("条形码解析成功(缩放x{}): {}", scale, result);
                    return result;
                }
            }

            // Pipeline 5: 原有预处理流水线（二值化 + 倾斜校正）作为兜底方案
            BufferedImage processed = preprocessImage(image);
            result = decodeBarcode(processed);
            if (result != null && !result.isEmpty()) {
                log.info("条形码解析成功(预处理): {}", result);
                return result;
            }

            throw new BusinessException(400, "未能识别到条形码，请上传清晰的条形码图片");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("条形码解析异常", e);
            throw new BusinessException(500, "条形码解析失败：" + e.getMessage());
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
     * 图片预处理流水线：灰度化 → 二值化 → 倾斜校正
     */
    private BufferedImage preprocessImage(BufferedImage original) {
        BufferedImage gray = toGrayscale(original);
        BufferedImage binary = binarize(gray);
        return deskew(binary);
    }

    /**
     * 拉普拉斯锐化增强，通过边缘增强强化条码的条空边界特征，
     * 提升 ZXing 对低对比度、模糊图片的特征提取能力
     */
    private BufferedImage sharpen(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage gray = ensureGrayType(source);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        // 拉普拉斯 3x3 锐化核：中心权值 5，邻域各 -1
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
     * 自适应缩放图片，解决条码区域在图片中占比过小导致的 ZXing 采样不足问题
     *
     * @param source 原始图片
     * @param scale  缩放比例
     */
    private BufferedImage scaleImage(BufferedImage source, double scale) {
        int newWidth = (int) Math.round(source.getWidth() * scale);
        int newHeight = (int) Math.round(source.getHeight() * scale);
        // 尺寸过小不缩放
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
     * 确保图片为 TYPE_BYTE_GRAY 格式（每像素 1 字节灰度值）。
     * TYPE_BYTE_BINARY 格式的打包像素在 BufferedImageLuminanceSource 中读取异常，
     * 会导致已二值化的图像在 ZXing 中再度被 HybridBinarizer 处理，条空边界特征完全丢失。
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

        // 计算灰度直方图
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = gray.getRaster().getSample(x, y, 0);
                histogram[pixel]++;
            }
        }

        // Otsu 算法计算最佳阈值
        int total = width * height;
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;
        double maxVariance = 0;
        int threshold = 128; // 默认阈值

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

        // 应用阈值
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
     * 倾斜校正：使用投影轮廓方差法检测倾斜角度并旋转矫正
     */
    private BufferedImage deskew(BufferedImage binary) {
        int width = binary.getWidth();
        int height = binary.getHeight();

        // 如果图片太小，跳过倾斜校正
        if (width < 50 || height < 50) {
            return binary;
        }

        double bestAngle = 0;
        double maxVariance = 0;
        int halfRange = (int) Math.round(DESKEW_ANGLE_RANGE / DESKEW_ANGLE_STEP);

        for (int i = -halfRange; i <= halfRange; i++) {
            double angle = i * DESKEW_ANGLE_STEP;
            double variance = computeProjectionVariance(binary, angle);
            if (variance > maxVariance) {
                maxVariance = variance;
                bestAngle = angle;
            }
        }

        if (Math.abs(bestAngle) < 0.1) {
            return binary; // 角度很小，无需旋转
        }

        log.debug("检测到倾斜角度: {}，执行旋转校正", String.format("%.2f", bestAngle));
        return rotateImage(binary, -bestAngle);
    }

    /**
     * 计算指定角度下水平投影的方差
     */
    private double computeProjectionVariance(BufferedImage binary, double angle) {
        int width = binary.getWidth();
        int height = binary.getHeight();
        double radians = Math.toRadians(angle);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // 采样计算投影，使用 stride 提升性能
        int stride = Math.max(1, Math.min(width, height) / 100);
        int[] projection = new int[height];
        int sampleCount = 0;

        for (int y = 0; y < height; y += stride) {
            for (int x = 0; x < width; x += stride) {
                // 计算旋转后的 y 坐标作为投影索引
                int projY = (int) Math.round(y * cos - x * sin + width * sin);
                if (projY >= 0 && projY < height) {
                    int pixel = binary.getRaster().getSample(x, y, 0);
                    if (pixel == 0) { // 黑色像素
                        projection[projY]++;
                    }
                    sampleCount++;
                }
            }
        }

        if (sampleCount == 0) return 0;

        // 计算均值
        double mean = 0;
        for (int v : projection) {
            mean += v;
        }
        mean /= height;

        // 计算方差
        double variance = 0;
        for (int v : projection) {
            variance += (v - mean) * (v - mean);
        }
        return variance / height;
    }

    /**
     * 旋转变换图片，输出 TYPE_BYTE_GRAY 格式（非 TYPE_BYTE_BINARY），
     * 使用双线性插值避免二值图像旋转产生的锯齿伪影破坏条码边缘
     */
    private BufferedImage rotateImage(BufferedImage source, double angle) {
        int width = source.getWidth();
        int height = source.getHeight();
        double radians = Math.toRadians(angle);

        // 计算旋转后的画布大小
        int newWidth = (int) Math.round(Math.abs(width * Math.cos(radians)) + Math.abs(height * Math.sin(radians)));
        int newHeight = (int) Math.round(Math.abs(height * Math.cos(radians)) + Math.abs(width * Math.sin(radians)));

        // 输出为 TYPE_BYTE_GRAY，避免 TYPE_BYTE_BINARY 的兼容性问题
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = rotated.createGraphics();
        // 白色背景填充
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newWidth, newHeight);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        AffineTransform transform = new AffineTransform();
        transform.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);
        transform.rotate(radians, width / 2.0, height / 2.0);
        g.setTransform(transform);
        g.drawImage(source, 0, 0, null);
        g.dispose();

        return rotated;
    }

    /**
     * 使用 ZXing MultiFormatReader 解码条形码，
     * 支持 CODE128 及 VUE 组件中定义的所有一维条形码格式
     */
    private String decodeBarcode(BufferedImage image) {
        if (image.getWidth() < 10 || image.getHeight() < 10) {
            return null;
        }

        // 先尝试对灰度和二值图像分别解码
        String result = tryDecode(image);
        if (result != null) return result;

        // 如果失败，尝试小幅旋转后的图像进行解码（多角度尝试）
        for (double angle : new double[]{-15, -10, -5, 5, 10, 15}) {
            BufferedImage rotated = rotateImage(image, angle);
            result = tryDecode(rotated);
            if (result != null) return result;
        }

        return null;
    }

    /**
     * 单次解码尝试。
     * 自动检测并转换 TYPE_BYTE_BINARY 格式为 TYPE_BYTE_GRAY，
     * 从而解决 BufferedImageLuminanceSource 对打包二值图像读取异常导致的识别失败问题。
     */
    private String tryDecode(BufferedImage image) {
        try {
            // 将 TYPE_BYTE_BINARY 转换为 TYPE_BYTE_GRAY，
            // 避免 ZXing 的 HybridBinarizer 对已二值化数据二次处理，导致条空边界特征丢失
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
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.CODE_93,
                    BarcodeFormat.EAN_8,
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.UPC_E,
                    BarcodeFormat.ITF,
                    BarcodeFormat.CODABAR
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
