package com.teamfoundry.backend.superadmin.controller.credentials;

import com.teamfoundry.backend.superadmin.dto.credential.admin.AdminCredentialDisableRequest;
import com.teamfoundry.backend.superadmin.dto.credential.admin.AdminCredentialRequest;
import com.teamfoundry.backend.superadmin.dto.credential.admin.AdminCredentialResponse;
import com.teamfoundry.backend.superadmin.dto.credential.admin.AdminCredentialUpdateRequest;
import com.teamfoundry.backend.superadmin.service.credentials.AdminCredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints consumidos pelo painel de super admin para gerir credenciais administrativas.
 */
@RestController
@RequestMapping("/api/super-admin/credentials")
@RequiredArgsConstructor
public class AdminCredentialController {

    private final AdminCredentialService adminCredentialService;

    @GetMapping("/admins")
    public List<AdminCredentialResponse> listAdminCredentials() {
        return adminCredentialService.listAdminCredentials();
    }

    @PostMapping("/admins")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminCredentialResponse createAdmin(@Valid @RequestBody AdminCredentialRequest request) {
        return adminCredentialService.createAdmin(request);
    }

    @PutMapping("/admins/{id}")
    public AdminCredentialResponse updateAdmin(@PathVariable int id,
                                               @Valid @RequestBody AdminCredentialUpdateRequest request) {
        return adminCredentialService.updateAdmin(id, request);
    }

    @DeleteMapping("/admins/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableAdmin(@PathVariable int id,
                             @Valid @RequestBody AdminCredentialDisableRequest request) {
        adminCredentialService.disableAdmin(id, request.superAdminPassword());
    }


}
