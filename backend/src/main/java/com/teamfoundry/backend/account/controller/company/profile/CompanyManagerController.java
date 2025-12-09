package com.teamfoundry.backend.account.controller.company.profile;

import com.teamfoundry.backend.account.dto.company.profile.CompanyManagerVerifyRequest;
import com.teamfoundry.backend.account.service.company.CompanyProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para envio/validação de código ao alterar email do responsável.
 */
@RestController
@RequestMapping(value = "/api/company/verification", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CompanyManagerController {

    private final CompanyProfileService companyProfileService;

    /**
     * Envia código de verificação para o novo email informado.
     */
    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void send(@RequestBody @Valid TargetEmail payload, Authentication authentication) {
        companyProfileService.sendVerificationCode(resolveEmail(authentication), payload.newEmail());
    }

    /**
     * Confirma o código e atualiza o responsável com o novo email.
     */
    @PostMapping(value = "/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object confirm(
            @RequestBody @Valid CompanyManagerVerifyRequest request,
            Authentication authentication) {
        return companyProfileService.verifyAndUpdateManager(resolveEmail(authentication), request);
    }

    private String resolveEmail(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Payload simples apenas com o novo email.
     */
    public record TargetEmail(@jakarta.validation.constraints.Email @jakarta.validation.constraints.NotBlank String newEmail) {}
}
