package com.teamfoundry.backend.superadmin.service.home;

import com.teamfoundry.backend.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeMediaService {

    private static final long MAX_FILE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final String CLOUDINARY_FOLDER = "home";

    private final CloudinaryService cloudinaryService;

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum ficheiro enviado.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Imagem excede o limite de 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas imagens são permitidas.");
        }

        try {
            CloudinaryService.UploadResult result = cloudinaryService.uploadBytes(
                    file.getBytes(),
                    CLOUDINARY_FOLDER,
                    file.getOriginalFilename()
            );
            return result.getUrl();
        } catch (Exception ex) {
            log.error("Falha ao guardar imagem da homepage no Cloudinary", ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível guardar o ficheiro na cloud."
            );
        }
    }
}

