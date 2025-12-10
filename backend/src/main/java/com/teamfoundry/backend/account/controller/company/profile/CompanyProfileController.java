package com.teamfoundry.backend.account.controller.company.profile;

import com.teamfoundry.backend.account.dto.company.profile.CompanyManagerUpdateRequest;
import com.teamfoundry.backend.account.dto.company.preferences.CompanyProfileResponse;
import com.teamfoundry.backend.account.service.company.CompanyProfileService;
import com.teamfoundry.backend.account.dto.company.profile.CompanyDeactivateAccountRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de perfil da empresa autenticada.
 */
@RestController
@RequestMapping(value = "/api/company/profile", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CompanyProfileController {

    private final CompanyProfileService companyProfileService;

    /**
     * Devolve os dados básicos da empresa (read-only) e do responsável (editável).
     */
    @GetMapping
    public CompanyProfileResponse getProfile(Authentication authentication) {
        return companyProfileService.getProfile(resolveEmail(authentication));
    }

    /**
     * Atualiza dados do responsável pela conta.
     */
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompanyProfileResponse updateManager(
            @Valid @RequestBody CompanyManagerUpdateRequest request,
            Authentication authentication) {
        return companyProfileService.updateManager(resolveEmail(authentication), request);
    }

    @PostMapping(value = "/deactivate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deactivateAccount(
            @Valid @RequestBody CompanyDeactivateAccountRequest request,
            Authentication authentication) {
        companyProfileService.deactivateAccount(resolveEmail(authentication), request);
    }

    private String resolveEmail(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
