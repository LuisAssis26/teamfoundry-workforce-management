package com.teamfoundry.backend.superadmin.controller.credentials;

import com.teamfoundry.backend.superadmin.dto.credential.company.CompanyApprovalRequest;
import com.teamfoundry.backend.superadmin.dto.credential.company.CompanyCredentialResponse;
import com.teamfoundry.backend.superadmin.service.credentials.CompanyCredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/credentials")
@RequiredArgsConstructor
public class CompanyCredentialController {

    private final CompanyCredentialService companyCredentialService;

    @GetMapping("/companies")
    public List<CompanyCredentialResponse> listPendingCompanyCredentials() {
        return companyCredentialService.listPendingCompanyCredentials();
    }

    @PostMapping("/companies/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveCompany(@PathVariable int id,
                               @Valid @RequestBody CompanyApprovalRequest request) {
        companyCredentialService.approveCompanyCredential(id, request.superAdminPassword());
    }

    @PostMapping("/companies/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectCompany(@PathVariable int id,
                              @Valid @RequestBody CompanyApprovalRequest request) {
        companyCredentialService.rejectCompanyCredential(id, request.superAdminPassword());
    }
}
