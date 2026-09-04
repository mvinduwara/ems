package com.retailhr.ems.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public class QrCodeGenerator {

    private static final int DEFAULT_SIZE = 300;

    private QrCodeGenerator() {
    }

    public static BitMatrix encode(String content, int size) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            return new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        } catch (WriterException e) {
            throw new IllegalStateException("Failed to encode QR code for content: " + content, e);
        }
    }

    public static void writeToPng(String content, Path outputPath, int size) {
        BitMatrix matrix = encode(content, size);
        try {
            MatrixToImageWriter.writeToPath(matrix, "PNG", outputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write QR code image to " + outputPath, e);
        }
    }

    public static void writeToPng(String content, Path outputPath) {
        writeToPng(content, outputPath, DEFAULT_SIZE);
    }

    public static Image toFxImage(String content, int size) {
        BitMatrix matrix = encode(content, size);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(MatrixToImageWriter.toBufferedImage(matrix), "PNG", baos);
            return new Image(new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render QR code to image", e);
        }
    }

    public static Image toFxImage(String content) {
        return toFxImage(content, DEFAULT_SIZE);
    }
}