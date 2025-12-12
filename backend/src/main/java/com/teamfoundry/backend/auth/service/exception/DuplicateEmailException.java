package com.teamfoundry.backend.auth.service.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends EmployeeRegistrationException {
    public DuplicateEmailException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
