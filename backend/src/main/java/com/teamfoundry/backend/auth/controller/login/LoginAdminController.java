package com.teamfoundry.backend.auth.controller.login;


import com.teamfoundry.backend.auth.dto.login.AdminLoginRequest;
import com.teamfoundry.backend.auth.service.login.AdminAuthService;
import com.teamfoundry.backend.common.dto.ApiErrorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller que expoe o endpoint de login de administrador com validacao via hash no banco.
 */
@RestController
@RequestMapping("/api/admin")
public class LoginAdminController {

    private final AdminAuthService adminAuthenticationService;

    public LoginAdminController(AdminAuthService adminAuthenticationService) {
        this.adminAuthenticationService = adminAuthenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginRequest request) {
        return adminAuthenticationService.authenticate(request.getUsername(), request.getPassword())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(this::buildUnauthorizedResponse);
    }


    private ResponseEntity<ApiErrorResponse> buildUnauthorizedResponse() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }
}
