package com.teamfoundry.backend.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private static final String RESOURCE_SEPARATOR = "::";
    private final Cloudinary cloudinary;

    public UploadResult uploadBase64(String rawBase64Payload, String folder, String originalName) {
        ensureConfigured();
        if (!StringUtils.hasText(rawBase64Payload)) {
            throw new IllegalArgumentException("Payload vazio.");
        }
        byte[] data = decodeBase64(rawBase64Payload);
        return uploadBytes(data, folder, originalName);
    }

    public UploadResult uploadBytes(byte[] data, String folder, String originalName) {
        ensureConfigured();
        try {
            String publicId = buildPublicId(folder, originalName);
            Map<String, Object> result = cloudinary.uploader().upload(data, ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicId,
                    "overwrite", true,
                    "resource_type", "auto"
            ));
            String returnedPublicId = (String) result.get("public_id");
            String resourceType = (String) result.getOrDefault("resource_type", "image");
            String storedPublicId = formatStoredId(resourceType, returnedPublicId);
            String secureUrl = (String) result.get("secure_url");
            if (!StringUtils.hasText(secureUrl)) {
                secureUrl = buildUrlInternal(storedPublicId);
            }
            return new UploadResult(storedPublicId, secureUrl);
        } catch (IOException e) {
            log.error("Falha ao enviar ficheiro para Cloudinary na pasta {}", folder, e);
            throw new IllegalStateException("N?o foi poss?vel guardar o ficheiro na cloud.");
        }
    }

    public void delete(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            return;
        }
        if (isDisabled()) {
            log.warn("Cloudinary n?o configurado; ignorar delete para {}", publicId);
            return;
        }
        ParsedId parsed = parseStoredId(publicId);
        if (parsed.legacyFormat()) {
            deleteForType(parsed.publicId(), "image");
            deleteForType(parsed.publicId(), "raw");
            return;
        }
        deleteForType(parsed.publicId(), parsed.resourceType());
    }

    /**
     * Tenta apagar um recurso Cloudinary a partir do URL seguro.
     * Utilizado para imagens de indústrias/parceiros onde apenas o URL foi guardado.
     * Se o URL não parecer ser do Cloudinary desta aplicação, não faz nada.
     */
    public void deleteByUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return;
        }
        if (!url.contains("res.cloudinary.com")) {
            return;
        }
        // Procura o segmento "/upload/" típico das URLs do Cloudinary
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx < 0) {
            return;
        }

        String path = url.substring(uploadIdx + "/upload/".length());
        int queryIdx = path.indexOf('?');
        if (queryIdx >= 0) {
            path = path.substring(0, queryIdx);
        }

        // Remove prefixo de versão (ex: v123456789/...)
        if (path.startsWith("v")) {
            int slashAfterVersion = path.indexOf('/');
            if (slashAfterVersion > 0) {
                path = path.substring(slashAfterVersion + 1);
            }
        }

        // Remove extensão do ficheiro (ex: .jpg, .png)
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx > 0) {
            path = path.substring(0, dotIdx);
        }

        if (!StringUtils.hasText(path)) {
            return;
        }

        String storedId = "image" + RESOURCE_SEPARATOR + path;
        delete(storedId);
    }

    private void deleteForType(String publicId, String resourceType) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", resourceType
            ));
        } catch (IOException e) {
            log.warn("Falha ao remover {} ({}) do Cloudinary: {}", publicId, resourceType, e.getMessage());
        }
    }

    private byte[] decodeBase64(String raw) {
        int commaIndex = raw.indexOf(',');
        String cleaned = commaIndex >= 0 ? raw.substring(commaIndex + 1) : raw;
        return Base64.getDecoder().decode(cleaned);
    }

    private String buildPublicId(String folder, String originalName) {
        String suffix = UUID.randomUUID().toString();
        String base = "file";
        if (StringUtils.hasText(originalName)) {
            String cleaned = originalName.replaceAll("\\s+", "_").replaceAll("[^A-Za-z0-9_\\-\\.]", "");
            if (StringUtils.hasText(cleaned)) {
                base = cleaned;
            }
        }
        return base + "_" + suffix;
    }

    private void ensureConfigured() {
        if (isDisabled()) {
            log.error("Cloudinary não está configurado. cloud_name={}, apiKey? {}",
                    cloudinary.config.cloudName, StringUtils.hasText(cloudinary.config.apiKey));
            throw new IllegalStateException("Cloudinary não está configurado. Defina CLOUDINARY_URL (cloudinary://<key>:<secret>@<cloud>).");
        }
    }

    private boolean isDisabled() {
        return !(StringUtils.hasText(cloudinary.config.cloudName)
                && StringUtils.hasText(cloudinary.config.apiKey)
                && StringUtils.hasText(cloudinary.config.apiSecret));
    }

    public String buildUrl(String storedPublicId) {
        if (!StringUtils.hasText(storedPublicId)) {
            return null;
        }
        return buildUrlInternal(storedPublicId);
    }

    private String formatStoredId(String resourceType, String publicId) {
        String type = StringUtils.hasText(resourceType) ? resourceType : "image";
        return type + RESOURCE_SEPARATOR + publicId;
    }

    private ParsedId parseStoredId(String stored) {
        if (!StringUtils.hasText(stored)) {
            return new ParsedId("image", stored, true);
        }
        int separatorIndex = stored.indexOf(RESOURCE_SEPARATOR);
        if (separatorIndex < 0) {
            return new ParsedId("image", stored, true);
        }
        String type = stored.substring(0, separatorIndex);
        String id = stored.substring(separatorIndex + RESOURCE_SEPARATOR.length());
        if (!StringUtils.hasText(type)) {
            type = "image";
        }
        return new ParsedId(type, id, false);
    }

    private String buildUrlInternal(String storedPublicId) {
        ParsedId parsed = parseStoredId(storedPublicId);
        if (parsed.legacyFormat()) {
            return cloudinary.url()
                    .secure(true)
                    .resourceType("raw")
                    .generate(parsed.publicId());
        }
        String resourceType = StringUtils.hasText(parsed.resourceType()) ? parsed.resourceType() : "image";
        return cloudinary.url()
                .secure(true)
                .resourceType(resourceType)
                .generate(parsed.publicId());
    }

    @Getter
    public static class UploadResult {
        private final String publicId;
        private final String url;

        public UploadResult(String publicId, String url) {
            this.publicId = publicId;
            this.url = url;
        }
    }

    private record ParsedId(String resourceType, String publicId, boolean legacyFormat) { }
}
