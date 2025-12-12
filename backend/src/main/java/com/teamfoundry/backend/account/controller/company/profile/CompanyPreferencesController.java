package com.teamfoundry.backend.account.controller.company.profile;

import com.teamfoundry.backend.account.dto.company.preferences.CompanyPreferencesListResponse;
import com.teamfoundry.backend.account.service.company.CompanyPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company/options")
@RequiredArgsConstructor
public class CompanyPreferencesController {

    private final CompanyPreferencesService companyPreferencesService;

    @GetMapping
    public ResponseEntity<CompanyPreferencesListResponse> listOptions() {
        return ResponseEntity.ok(companyPreferencesService.loadOptions());
    }
}
