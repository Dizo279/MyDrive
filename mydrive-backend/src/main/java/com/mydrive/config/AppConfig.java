package com.mydrive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class AppConfig {

    @Value("${app.storage.path}")
    private String storagePath;

    /**
     * Tạo thư mục uploads nếu chưa tồn tại khi app khởi động
     */
    @Bean
    public Path storageLocation() throws IOException {
        Path path = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(path);
        return path;
    }

    /**
     * Cấu hình CORS cho phép Angular (port 4200) gọi API
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                            "http://localhost:4200",  // Angular dev server
                            "http://localhost"        // Production
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}