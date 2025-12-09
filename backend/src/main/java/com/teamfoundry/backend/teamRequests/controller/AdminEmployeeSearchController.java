package com.teamfoundry.backend.teamRequests.controller;

import com.teamfoundry.backend.teamRequests.dto.search.AdminEmployeeSearchResponse;
import com.teamfoundry.backend.teamRequests.service.AdminEmployeeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin/candidates")
@RequiredArgsConstructor
public class AdminEmployeeSearchController {

    private final AdminEmployeeSearchService adminEmployeeSearchService;

    @GetMapping("/search")
    public List<AdminEmployeeSearchResponse> search(
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "areas", required = false) List<String> areas,
            @RequestParam(name = "skills", required = false) List<String> skills
    ) {
        List<String> safeAreas = areas != null ? areas : Collections.emptyList();
        List<String> safeSkills = skills != null ? skills : Collections.emptyList();
        return adminEmployeeSearchService.search(role, safeAreas, safeSkills);
    }
}
