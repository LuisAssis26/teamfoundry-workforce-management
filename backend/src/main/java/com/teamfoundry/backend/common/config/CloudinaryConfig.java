package com.teamfoundry.backend.common.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.url:${CLOUDINARY_URL:}}")
    private String cloudinaryUrl;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        String url = StringUtils.hasText(cloudinaryUrl) ? cloudinaryUrl.trim() : System.getenv("CLOUDINARY_URL");
        if (StringUtils.hasText(url)) {
            log.info("DEBUG Cloudinary API key associada: {}", extractApiKeyFromUrl(url));
            log.info("Cloudinary configurado via URL para cloud '{}', usando URL '{}***@{}'",
                    extractCloudName(url), maskKey(url), extractDomain(url));
            return new Cloudinary(url);
        }

        if (StringUtils.hasText(cloudName) && StringUtils.hasText(apiKey) && StringUtils.hasText(apiSecret)) {
            log.info("DEBUG Cloudinary API key associada: {}", apiKey);
            log.info("Cloudinary configurado via credenciais explicitas para cloud '{}' , usando apiKey '{}***'",
                    cloudName, maskKey(apiKey + ":dummy"));
            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
        }

        log.info("DEBUG Cloudinary API key associada: (nao configurada)");
        log.warn("Cloudinary URL em falta. Defina CLOUDINARY_URL (cloudinary://<key>:<secret>@<cloud>) ou cloudinary.cloud-name/api-key/api-secret. Uploads falharao.");
        return new Cloudinary(); // sem credenciais; service vai impedir upload
    }

    private String extractCloudName(String url) {
        if (!StringUtils.hasText(url) || !url.contains("@")) return "desconhecido";
        return url.substring(url.indexOf('@') + 1);
    }

    private String maskKey(String url) {
        if (!StringUtils.hasText(url)) return "??";
        int schemeIdx = url.indexOf("://");
        int colonIdx = url.indexOf(':', schemeIdx + 3);
        if (schemeIdx < 0 || colonIdx < 0) return "??";
        String key = url.substring(schemeIdx + 3, colonIdx);
        if (key.length() <= 4) return "****";
        return key.substring(0, 4);
    }

    private String extractDomain(String url) {
        if (!StringUtils.hasText(url) || !url.contains("@")) return "?";
        return url.substring(url.indexOf('@') + 1);
    }

    private String extractApiKeyFromUrl(String url) {
        if (!StringUtils.hasText(url)) return "(vazio)";
        int schemeIdx = url.indexOf("://");
        int colonIdx = url.indexOf(':', schemeIdx + 3);
        if (schemeIdx < 0 || colonIdx < 0) return "(mal formatado)";
        return url.substring(schemeIdx + 3, colonIdx);
    }
}
