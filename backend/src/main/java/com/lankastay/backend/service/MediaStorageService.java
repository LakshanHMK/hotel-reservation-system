package com.lankastay.backend.service;

import com.lankastay.backend.dto.media.MediaUploadResponse;
import com.lankastay.backend.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaStorageService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private final Path uploadLocation;

    public MediaStorageService(@Value("${file.upload-dir:uploads/destinations}") String uploadDir) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage directory: " + uploadDir, e);
        }
    }

    public MediaUploadResponse storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Please upload a valid, non-empty image file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessRuleException("File size exceeds maximum limit of 5 MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleException("Unsupported file format. Only JPG, PNG, and WebP images are allowed.");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.png");

        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BusinessRuleException("Invalid filename path traversal attempt detected.");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        } else {
            if ("image/jpeg".equals(contentType)) extension = ".jpg";
            else if ("image/png".equals(contentType)) extension = ".png";
            else if ("image/webp".equals(contentType)) extension = ".webp";
        }

        String generatedFilename = "dest_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        try {
            Path targetLocation = this.uploadLocation.resolve(generatedFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            String publicUrl = "/uploads/destinations/" + generatedFilename;
            return new MediaUploadResponse(publicUrl, generatedFilename, file.getSize(), contentType);
        } catch (IOException ex) {
            throw new BusinessRuleException("Failed to store uploaded file on server: " + ex.getMessage());
        }
    }
}
