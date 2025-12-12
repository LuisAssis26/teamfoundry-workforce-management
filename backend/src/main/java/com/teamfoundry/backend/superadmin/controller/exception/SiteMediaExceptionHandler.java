package com.teamfoundry.backend.superadmin.controller.exception;

import com.teamfoundry.backend.superadmin.controller.website.WebsiteMediaController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice(assignableTypes = WebsiteMediaController.class)
public class SiteMediaExceptionHandler {

    private static final String LIMIT_MESSAGE = "A imagem não pode exceder 5MB.";

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUpload(MaxUploadSizeExceededException ignored) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(LIMIT_MESSAGE);
    }
}
