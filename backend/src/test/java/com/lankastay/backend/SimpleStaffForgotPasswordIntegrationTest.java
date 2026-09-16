package com.lankastay.backend;

import com.lankastay.backend.entity.StaffRole;
import com.lankastay.backend.entity.StaffStatus;
import com.lankastay.backend.entity.StaffUser;
import com.lankastay.backend.repository.StaffUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimpleStaffForgotPasswordIntegrationTest {
    private static final String DATABASE_NAME = "staff_reset_" + UUID.randomUUID().toString().replace("-", "");
    private static final String EMAIL = "manager.forgot@lankastay.test";
    private static final String OLD_PASSWORD = "OldManager1!";
    private static final String NEW_PASSWORD = "NewManager2@";

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + DATABASE_NAME + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class FastPasswordConfig {
        @Bean @Primary PasswordEncoder fastPasswordEncoder() { return new BCryptPasswordEncoder(4); }
    }

    @Autowired MockMvc mvc;
    @Autowired StaffUserRepository users;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        users.deleteAll();
        StaffUser manager = new StaffUser();
        manager.setEmail(EMAIL);
        manager.setFirstName("Forgot");
        manager.setLastName("Manager");
        manager.setRole(StaffRole.MANAGER);
        manager.setStatus(StaffStatus.ACTIVE);
        manager.setMustChangePassword(false);
        manager.setPasswordHash(encoder.encode(OLD_PASSWORD));
        users.saveAndFlush(manager);
    }

    @Test
    void managerEmailCanBeCheckedAndUnknownEmailIsReported() throws Exception {
        mvc.perform(post("/api/v1/auth/forgot-password/check-email").with(csrf())
                        .contentType("application/json").content("{\"email\":\"MANAGER.FORGOT@LANKASTAY.TEST\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(true));
        mvc.perform(post("/api/v1/auth/forgot-password/check-email").with(csrf())
                        .contentType("application/json").content("{\"email\":\"missing.manager@lankastay.test\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    void managerPasswordResetStoresHashAndChangesLoginCredential() throws Exception {
        mvc.perform(post("/api/v1/auth/forgot-password/change-password").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"newPassword\":\"" + NEW_PASSWORD + "\",\"confirmPassword\":\"Different3#\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/auth/forgot-password/change-password").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"newPassword\":\"" + NEW_PASSWORD + "\",\"confirmPassword\":\"" + NEW_PASSWORD + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Password changed successfully."));

        StaffUser saved = users.findByEmail(EMAIL).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(NEW_PASSWORD);
        assertThat(encoder.matches(OLD_PASSWORD, saved.getPasswordHash())).isFalse();
        assertThat(encoder.matches(NEW_PASSWORD, saved.getPasswordHash())).isTrue();

        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + OLD_PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + NEW_PASSWORD + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("MANAGER"));
    }
}
