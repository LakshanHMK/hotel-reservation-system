package com.lankastay.backend;

import com.lankastay.backend.dto.media.MediaUploadResponse;
import com.lankastay.backend.exception.BusinessRuleException;
import com.lankastay.backend.service.MediaStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MediaStorageServiceTest {

    @Autowired
    private MediaStorageService mediaStorageService;

    @Test
    @DisplayName("1. Successful image upload returns stable public URL")
    void testSuccessfulImageUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-cover.png",
                "image/png",
                "dummy image content bytes".getBytes()
        );

        MediaUploadResponse response = mediaStorageService.storeFile(file);

        assertNotNull(response);
        assertNotNull(response.getUrl());
        assertTrue(response.getUrl().startsWith("/uploads/destinations/dest_"));
        assertTrue(response.getUrl().endsWith(".png"));
    }

    @Test
    @DisplayName("2. Unsupported MIME type rejected")
    void testUnsupportedMimeTypeRejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/x-msdownload",
                "binary content".getBytes()
        );

        assertThrows(BusinessRuleException.class, () -> {
            mediaStorageService.storeFile(file);
        });
    }

    @Test
    @DisplayName("3. Path traversal filename attempt rejected")
    void testPathTraversalRejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../secret.png",
                "image/png",
                "content".getBytes()
        );

        assertThrows(BusinessRuleException.class, () -> {
            mediaStorageService.storeFile(file);
        });
    }
}
