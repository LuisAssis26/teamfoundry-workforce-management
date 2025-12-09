package com.teamfoundry.backend.account.controller.company.teamRequests;

import com.teamfoundry.backend.account.dto.company.teamRequests.CompanyTeamRequestCreateRequest;
import com.teamfoundry.backend.account.dto.company.teamRequests.CompanyTeamRequestResponse;
import com.teamfoundry.backend.account.service.company.CompanyTeamRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints das requisições criadas pela empresa autenticada.
 */
@RestController
@RequestMapping(value = "/api/company/requests", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CompanyTeamRequestController {

    private final CompanyTeamRequestService companyTeamRequestService;

    /**
     * Lista todas as requisições da empresa, já classificadas para uso em tabs.
     */
    @GetMapping
    public List<CompanyTeamRequestResponse> list(Authentication authentication) {
        return companyTeamRequestService.listCompanyRequests(resolveEmail(authentication));
    }

    /**
     * Cria uma nova requisição.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CompanyTeamRequestResponse create(
            @Valid @RequestBody CompanyTeamRequestCreateRequest request,
            Authentication authentication) {
        return companyTeamRequestService.createRequest(resolveEmail(authentication), request);
    }

    private String resolveEmail(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
