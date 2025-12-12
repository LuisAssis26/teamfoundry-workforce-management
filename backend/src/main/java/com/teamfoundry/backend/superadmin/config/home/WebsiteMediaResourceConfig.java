package com.teamfoundry.backend.superadmin.config.home;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebsiteMediaResourceConfig implements WebMvcConfigurer {

    private final HomeMediaProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String handlerPattern = properties.getPublicPath() + "/**";
        String location = properties.getStorageDir().toUri().toString();
        registry.addResourceHandler(handlerPattern)
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(6)).cachePublic());
    }
}
