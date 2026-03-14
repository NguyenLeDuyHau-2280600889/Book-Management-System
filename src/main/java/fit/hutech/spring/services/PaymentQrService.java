package fit.hutech.spring.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

@Service
public class PaymentQrService {
    private static final String QR_FILE_NAME = "payment-qr.png";
    private static final String NOTE_FILE_NAME = "payment-qr-note.txt";

    private final Path paymentDir;

    public PaymentQrService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.paymentDir = Paths.get(uploadDir, "payment").toAbsolutePath().normalize();
    }

    public String getQrImagePath() {
        Path qr = paymentDir.resolve(QR_FILE_NAME);
        if (Files.exists(qr)) {
            return "/uploads/payment/" + QR_FILE_NAME;
        }
        return "";
    }

    public String getQrNote() {
        Path noteFile = paymentDir.resolve(NOTE_FILE_NAME);
        if (!Files.exists(noteFile)) {
            return "";
        }
        try {
            return Files.readString(noteFile, StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            return "";
        }
    }

    public void saveQr(MultipartFile qrFile, String note) throws IOException {
        Files.createDirectories(paymentDir);
        if (qrFile != null && !qrFile.isEmpty()) {
            if (!isImageFile(qrFile)) {
                throw new IllegalArgumentException("Chi chap nhan tep anh (jpg, jpeg, png, webp).");
            }
            Files.copy(qrFile.getInputStream(), paymentDir.resolve(QR_FILE_NAME), StandardCopyOption.REPLACE_EXISTING);
        }
        Files.writeString(paymentDir.resolve(NOTE_FILE_NAME), note == null ? "" : note.trim(), StandardCharsets.UTF_8);
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return true;
        }
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            return false;
        }
        String name = original.toLowerCase(Locale.ROOT);
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp");
    }
}
