package com.lankastay.backend;

import com.lankastay.backend.dto.destination.DestinationCreateRequest;
import com.lankastay.backend.dto.destination.DestinationUpdateRequest;
import com.lankastay.backend.entity.DestinationStatus;
import com.lankastay.backend.service.DestinationService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "RUN_MYSQL_INTEGRATION_TESTS", matches = "true")
class DestinationMySqlPersistenceIT {
    private static final Path UPLOAD_DIRECTORY = Path.of("target", "destination-mysql-it-uploads").toAbsolutePath().normalize();

    @DynamicPropertySource
    static void mysql(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> System.getenv("DB_URL"));
        properties.add("spring.datasource.username", () -> firstConfigured("DB_USER", "DB_USERNAME"));
        properties.add("spring.datasource.password", () -> System.getenv("DB_PASSWORD"));
        properties.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        properties.add("spring.flyway.enabled", () -> "true");
        properties.add("file.upload-dir", () -> UPLOAD_DIRECTORY.toString());
    }

    private static String firstConfigured(String primary, String fallback) {
        String value = System.getenv(primary);
        return value == null || value.isBlank() ? System.getenv(fallback) : value;
    }

    @Autowired DestinationService destinations;
    @Autowired EntityManager entityManager;
    @Autowired MockMvc mvc;

    @Test
    void multipartUploadAndDestinationCreateUpdateSurviveFlushAndReloadInMySql() throws Exception {
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", imageBytes);
        String uploadBody = mvc.perform(multipart("/api/media/upload")
                        .file(new MockMultipartFile("file", "destination.png", "image/png", imageBytes.toByteArray()))
                        .with(user("destination-mysql-it").roles("MANAGER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        String imageUrl = new tools.jackson.databind.ObjectMapper().readTree(uploadBody).get("url").asString();
        Path storedImage = UPLOAD_DIRECTORY.resolve(imageUrl.substring("/uploads/".length())).normalize();
        assertTrue(storedImage.startsWith(UPLOAD_DIRECTORY));
        assertTrue(Files.isRegularFile(storedImage), "the multipart upload must be written to the configured upload directory");
        mvc.perform(get(imageUrl)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));

        String uniqueName = "Persistence Test " + System.nanoTime();
        DestinationCreateRequest create = new DestinationCreateRequest();
        create.setName(uniqueName);
        create.setShortDescription("MySQL destination persistence verification.");
        create.setFullDescription("A rollback-only integration record used to verify the real MySQL mapping.");
        create.setRegion("Southern Province");
        create.setDistrict("Galle");
        create.setLatitude(6.0329);
        create.setLongitude(80.2168);
        create.setMainImage(imageUrl);
        create.setStatus(DestinationStatus.DRAFT);
        create.setThemeKeys(Set.of("COAST"));
        create.setHighlights(List.of("Persistence verification"));

        var created = destinations.createDestination(create);
        entityManager.flush();
        entityManager.clear();
        var reloaded = destinations.getDestinationById(created.getId());
        assertEquals(uniqueName, reloaded.getName());
        assertEquals(imageUrl, reloaded.getMainImage());

        DestinationUpdateRequest update = new DestinationUpdateRequest();
        update.setShortDescription("Updated and reloaded from MySQL.");
        update.setVersion(reloaded.getVersion());
        destinations.updateDestination(created.getId(), update);
        entityManager.flush();
        entityManager.clear();
        assertEquals("Updated and reloaded from MySQL.", destinations.getDestinationById(created.getId()).getShortDescription());
    }
}
