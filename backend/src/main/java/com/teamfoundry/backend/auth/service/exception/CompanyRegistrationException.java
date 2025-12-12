package com.teamfoundry.backend.auth.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CompanyRegistrationException extends RuntimeException {

    private final HttpStatus status;

    public CompanyRegistrationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
