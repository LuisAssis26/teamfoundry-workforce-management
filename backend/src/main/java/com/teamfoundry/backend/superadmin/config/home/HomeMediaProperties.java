package com.teamfoundry.backend.superadmin.config.home;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Component
public class HomeMediaProperties {

    private final Path storageDir;
    private final String publicPath;

    public HomeMediaProperties(
            @Value("${app.site.media.storage-dir:uploads/site}") String storageDir,
            @Value("${app.site.media.public-path:/media/site}") String publicPath
    ) {
        this.storageDir = Paths.get(storageDir).toAbsolutePath().normalize();
        this.publicPath = normalizePublicPath(publicPath);
    }

    private String normalizePublicPath(String path) {
        if (path == null || path.isBlank()) {
            return "/media/site";
        }
        String normalized = path.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
