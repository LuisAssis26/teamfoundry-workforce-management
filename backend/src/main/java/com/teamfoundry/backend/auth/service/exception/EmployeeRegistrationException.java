package com.teamfoundry.backend.auth.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmployeeRegistrationException extends RuntimeException {

    private final HttpStatus status;

    public EmployeeRegistrationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
