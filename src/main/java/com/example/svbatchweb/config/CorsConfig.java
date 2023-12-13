package com.example.svbatchweb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "http://43.202.10,41:8080")
                .allowedMethods("GET","POST","PUT","DELETE")
                .allowedHeaders("Content-Type")
                .allowCredentials(true);
    }
}