package com.retailhr.ems.util;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class QrCodeScanner {

    private final Webcam webcam;
    private final ScheduledExecutorService scheduler;
    private final Reader reader;
    private final Map<DecodeHintType, Object> hints;
    private volatile boolean scanning = false;
    private String lastDecodedText = null;
    private long lastDecodedAt = 0L;
    private static final long DEDUPE_WINDOW_MS = 3000L;

    public QrCodeScanner(Webcam webcam) {
        this.webcam = webcam;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.reader = new MultiFormatReader();
        this.hints = new EnumMap<>(DecodeHintType.class);
        this.hints.put(DecodeHintType.POSSIBLE_FORMATS, java.util.List.of(BarcodeFormat.QR_CODE));
        this.hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    }

    public void start(Consumer<String> onDecoded, Consumer<Throwable> onError) {
        if (!webcam.isOpen()) {
            webcam.open();
        }
        scanning = true;
        scheduler.scheduleWithFixedDelay(() -> {
            if (!scanning) {
                return;
            }
            try {
                BufferedImage image = webcam.getImage();
                if (image == null) {
                    return;
                }
                String decoded = decode(image);
                if (decoded != null) {
                    long now = System.currentTimeMillis();
                    boolean isDuplicate = decoded.equals(lastDecodedText) && (now - lastDecodedAt) < DEDUPE_WINDOW_MS;
                    if (!isDuplicate) {
                        lastDecodedText = decoded;
                        lastDecodedAt = now;
                        onDecoded.accept(decoded);
                    }
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        }, 0, 300, TimeUnit.MILLISECONDS);
    }

    private String decode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = reader.decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        } finally {
            reader.reset();
        }
    }

    public void stop() {
        scanning = false;
        scheduler.shutdown();
        if (webcam.isOpen()) {
            webcam.close();
        }
    }

    public WebcamPanel createPanel() {
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(false);
        panel.setDisplayDebugInfo(false);
        panel.setImageSizeDisplayed(false);
        panel.setMirrored(true);
        return panel;
    }

    public static Webcam getDefaultWebcam() {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new IllegalStateException("No webcam detected");
        }
        return webcam;
    }
}