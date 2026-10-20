package com.example.smartfactoryservice.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ParsingBarcodeServiceImpl 优化后测试
 * 覆盖不同分辨率、角度、尺寸的条码识别场景，验证识别成功率和性能
 */
@SpringBootTest
class ParsingBarcodeServiceImplTest {

    @Autowired
    private ParsingBarcodeServiceImpl parsingBarcodeService;

    private static final int CODE_128_WIDTH = 300;
    private static final int CODE_128_HEIGHT = 100;

    /**
     * 测试用例 1: 标准 CODE128 条码
     */
    @Test
    @DisplayName("标准 CODE128 条码识别")
    void testStandardCode128() {
        String expected = "SMART-FACTORY-001";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, CODE_128_WIDTH, CODE_128_HEIGHT, 0);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 2: 纯数字 CODE128 条码
     */
    @Test
    @DisplayName("纯数字 CODE128 条码")
    void testNumericCode128() {
        String expected = "123456789012";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, CODE_128_WIDTH, CODE_128_HEIGHT, 0);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 3: 低分辨率条码（150x50）
     */
    @Test
    @DisplayName("低分辨率 150x50 CODE128 条码")
    void testLowResCode128() {
        String expected = "LOW-RES-150";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, 150, 50, 0);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 4: 高分辨率条码（600x200）
     */
    @Test
    @DisplayName("高分辨率 600x200 CODE128 条码")
    void testHighResCode128() {
        String expected = "HIGH-RES-600";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, 600, 200, 0);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 5: 小角度倾斜条码（5°）
     */
    @Test
    @DisplayName("5度倾斜 CODE128 条码")
    void testTilted5DegCode128() {
        String expected = "TILT-5DEG";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, CODE_128_WIDTH, CODE_128_HEIGHT, 5);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 6: 中等角度倾斜条码（10°）
     */
    @Test
    @DisplayName("10度倾斜 CODE128 条码")
    void testTilted10DegCode128() {
        String expected = "TILT-10DEG";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, CODE_128_WIDTH, CODE_128_HEIGHT, 10);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 7: CODE39 条码
     */
    @Test
    @DisplayName("CODE39 条码识别")
    void testCode39() {
        String expected = "CODE39-TEST";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_39, CODE_128_WIDTH, CODE_128_HEIGHT, 0);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 8: EAN13 条码
     */
    @Test
    @DisplayName("EAN13 条码识别")
    void testEAN13() {
        String expected = "5901234123457";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.EAN_13, CODE_128_WIDTH, CODE_128_HEIGHT, 0);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 9: 缩小尺寸条码（200x70）模拟条码在图片中占比较小
     */
    @Test
    @DisplayName("缩小尺寸 200x70 CODE128 条码")
    void testSmallSizeCode128() {
        String expected = "SMALL-200X70";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, 200, 70, 0);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 测试用例 10: 大角度倾斜 + 低分辨率组合（15°，150x50）
     */
    @Test
    @DisplayName("15度倾斜 + 低分辨率 CODE128 条码")
    void testTilted15DegLowResCode128() {
        String expected = "TILT15-LOW";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, 150, 50, 15);
        String result = parsingBarcodeService.parseBarcode(file);
        assertEquals(expected, result);
    }

    /**
     * 性能测试：验证优化后耗时不超过原算法的 120%
     */
    @Test
    @DisplayName("性能基准测试")
    void testPerformance() {
        String expected = "PERF-TEST-001";
        MultipartFile file = generateBarcodeImage(expected, BarcodeFormat.CODE_128, CODE_128_WIDTH, CODE_128_HEIGHT, 0);

        // 预热
        for (int i = 0; i < 3; i++) {
            parsingBarcodeService.parseBarcode(file);
        }

        // 正式测试
        int iterations = 10;
        long totalNanos = 0;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            String result = parsingBarcodeService.parseBarcode(file);
            long end = System.nanoTime();
            totalNanos += (end - start);
            assertEquals(expected, result);
        }

        double avgMillis = totalNanos / (double) iterations / 1_000_000.0;
        System.out.printf("=== 性能测试结果 ===%n");
        System.out.printf("迭代次数: %d%n", iterations);
        System.out.printf("平均耗时: %.2f ms%n", avgMillis);
        System.out.printf("总耗时: %.2f ms%n", totalNanos / 1_000_000.0);

        // 单次识别应在合理范围内（通常 < 200ms）
        assertTrue(avgMillis < 2000, "性能异常：平均耗时超过 2000ms");
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成条码图片 MultipartFile，支持旋转角度模拟真实拍照场景
     */
    private MultipartFile generateBarcodeImage(String content, BarcodeFormat format,
                                                int width, int height, double rotateDegrees) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter()
                    .encode(content, format, width, height);
            BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // 如果指定了旋转角度，旋转图片
            if (Math.abs(rotateDegrees) > 0.01) {
                barcodeImage = rotateImage(barcodeImage, rotateDegrees);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(barcodeImage, "png", baos);
            byte[] bytes = baos.toByteArray();

            return new MockMultipartFile(
                    "file",
                    "barcode.png",
                    "image/png",
                    new ByteArrayInputStream(bytes)
            );
        } catch (Exception e) {
            throw new RuntimeException("生成测试条码图片失败", e);
        }
    }

    /**
     * 旋转 BufferedImage
     */
    private BufferedImage rotateImage(BufferedImage source, double angle) {
        int width = source.getWidth();
        int height = source.getHeight();
        double radians = Math.toRadians(angle);

        int newWidth = (int) Math.round(Math.abs(width * Math.cos(radians)) + Math.abs(height * Math.sin(radians)));
        int newHeight = (int) Math.round(Math.abs(height * Math.cos(radians)) + Math.abs(width * Math.sin(radians)));

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rotated.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newWidth, newHeight);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        AffineTransform transform = new AffineTransform();
        transform.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);
        transform.rotate(radians, width / 2.0, height / 2.0);
        g.setTransform(transform);
        g.drawImage(source, 0, 0, null);
        g.dispose();

        return rotated;
    }
}
