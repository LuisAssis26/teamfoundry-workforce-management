package com.teamfoundry.backend.auth.controller.register;

import com.teamfoundry.backend.common.dto.GenericResponse;
import com.teamfoundry.backend.auth.dto.register.employee.Step1Request;
import com.teamfoundry.backend.auth.dto.register.employee.Step2Request;
import com.teamfoundry.backend.auth.dto.register.employee.Step3Request;
import com.teamfoundry.backend.auth.dto.register.employee.Step4Request;
import com.teamfoundry.backend.auth.service.register.EmployeeRegistrationService;
import com.teamfoundry.backend.auth.service.exception.EmployeeRegistrationException;
import com.teamfoundry.backend.auth.dto.register.employee.VerificationResendRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/employee", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class EmployeeRegistrationController {

    private final EmployeeRegistrationService registrationService;

    @PostMapping("/register/step1")
    public ResponseEntity<GenericResponse> registerStep1(@Valid @RequestBody Step1Request request) {
        GenericResponse response = registrationService.handleStep1(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/step2")
    public ResponseEntity<GenericResponse> registerStep2(@Valid @RequestBody Step2Request request) {
        GenericResponse response = registrationService.handleStep2(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/step3")
    public ResponseEntity<GenericResponse> registerStep3(@Valid @RequestBody Step3Request request) {
        GenericResponse response = registrationService.handleStep3(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/step4")
    public ResponseEntity<GenericResponse> registerStep4(@Valid @RequestBody Step4Request request) {
        GenericResponse response = registrationService.handleStep4(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Reenvia o código de verificação para o e-mail indicado.
     */
    @PostMapping("/verification/send")
    public ResponseEntity<GenericResponse> resendVerification(@Valid @RequestBody VerificationResendRequest request) {
        return ResponseEntity.ok(registrationService.resendVerificationCode(request));
    }

    @ExceptionHandler(EmployeeRegistrationException.class)
    public ResponseEntity<GenericResponse> handleRegistrationException(EmployeeRegistrationException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(GenericResponse.failure(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidationErrors(MethodArgumentNotValidException exception) {
        String errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Erro de validação no registo: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(GenericResponse.failure(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleUnexpectedExceptions(Exception exception) {
        log.error("Erro inesperado no fluxo de registo", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponse.failure("Ocorreu um erro inesperado. Tente novamente."));
    }
}
