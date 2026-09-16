package com.lankastay.backend;

import com.lankastay.backend.entity.CustomerUser;
import com.lankastay.backend.repository.CustomerUserRepository;
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
class SimpleForgotPasswordIntegrationTest {
    private static final String DATABASE_NAME = "simple_reset_" + UUID.randomUUID().toString().replace("-", "");
    private static final String EMAIL = "forgot.customer@lankastay.test";
    private static final String OLD_PASSWORD = "OldPassword1!";
    private static final String NEW_PASSWORD = "NewPassword2@";

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:" + DATABASE_NAME + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class FastPasswordConfig {
        @Bean @Primary PasswordEncoder fastPasswordEncoder() { return new BCryptPasswordEncoder(4); }
    }

    @Autowired MockMvc mvc;
    @Autowired CustomerUserRepository customers;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        customers.deleteAll();
        CustomerUser customer = new CustomerUser();
        customer.setEmail(EMAIL);
        customer.setFirstName("Forgot");
        customer.setLastName("Customer");
        customer.setPasswordHash(encoder.encode(OLD_PASSWORD));
        customer.setStatus("ACTIVE");
        customers.saveAndFlush(customer);
    }

    @Test
    void existingAndUnknownEmailsReturnExpectedExistence() throws Exception {
        mvc.perform(post("/api/v1/customer/auth/forgot-password/check-email").with(csrf())
                        .contentType("application/json").content("{\"email\":\"FORGOT.CUSTOMER@LANKASTAY.TEST\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(true));
        mvc.perform(post("/api/v1/customer/auth/forgot-password/check-email").with(csrf())
                        .contentType("application/json").content("{\"email\":\"missing@lankastay.test\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(false));
        mvc.perform(post("/api/v1/customer/auth/forgot-password/change-password").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"missing@lankastay.test\",\"newPassword\":\"" + NEW_PASSWORD + "\",\"confirmPassword\":\"" + NEW_PASSWORD + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No account found with this email."));
    }

    @Test
    void mismatchIsRejectedAndSuccessfulChangeReplacesOnlyTheHash() throws Exception {
        mvc.perform(post("/api/v1/customer/auth/forgot-password/change-password").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"newPassword\":\"" + NEW_PASSWORD + "\",\"confirmPassword\":\"Different3#\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/v1/customer/auth/forgot-password/change-password").with(csrf())
                        .contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"newPassword\":\"" + NEW_PASSWORD + "\",\"confirmPassword\":\"" + NEW_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully."));

        CustomerUser saved = customers.findByEmail(EMAIL).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(NEW_PASSWORD);
        assertThat(encoder.matches(OLD_PASSWORD, saved.getPasswordHash())).isFalse();
        assertThat(encoder.matches(NEW_PASSWORD, saved.getPasswordHash())).isTrue();

        mvc.perform(post("/api/v1/customer/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + OLD_PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/customer/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + NEW_PASSWORD + "\"}"))
                .andExpect(status().isOk());
    }
}
