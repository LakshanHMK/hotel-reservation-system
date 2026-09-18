package com.lankastay.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadPathUri = uploadPath.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPathUri.endsWith("/") ? uploadPathUri : uploadPathUri + "/")
                .resourceChain(true)
                .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver() {
                    @Override
                    protected org.springframework.core.io.Resource getResource(String path, org.springframework.core.io.Resource location)
                            throws java.io.IOException {
                        if (!path.matches("(?i)[a-z0-9_/-]+\\.(jpg|jpeg|png|webp)")) return null;
                        return super.getResource(path, location);
                    }
                });
    }
}
