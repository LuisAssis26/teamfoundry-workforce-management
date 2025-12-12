package com.teamfoundry.backend.auth.controller.register;

import com.teamfoundry.backend.common.dto.GenericResponse;
import com.teamfoundry.backend.auth.dto.register.company.CompanyRegistrationRequest;
import com.teamfoundry.backend.auth.service.register.CompanyRegistrationService;
import com.teamfoundry.backend.auth.service.exception.CompanyRegistrationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/company/register")
@RequiredArgsConstructor
@Slf4j
public class CompanyRegistrationController {

    private final CompanyRegistrationService companyRegistrationService;

    @PostMapping
    public ResponseEntity<GenericResponse> register(@Valid @RequestBody CompanyRegistrationRequest request) {
        GenericResponse response = companyRegistrationService.registerCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(CompanyRegistrationException.class)
    public ResponseEntity<GenericResponse> handleBusinessError(CompanyRegistrationException ex) {
        return ResponseEntity.status(ex.getStatus()).body(GenericResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidationErrors(MethodArgumentNotValidException exception) {
        String errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Erro de validação no registo de empresa: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GenericResponse.failure(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleUnexpected(Exception exception) {
        log.error("Erro inesperado no registo de empresa", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponse.failure("Ocorreu um erro inesperado. Tente novamente."));
    }
}
