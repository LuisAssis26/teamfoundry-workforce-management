package com.teamfoundry.backend.teamRequests.controller;

import com.teamfoundry.backend.teamRequests.dto.search.AdminEmployeeProfileResponse;
import com.teamfoundry.backend.teamRequests.service.AdminEmployeeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Perfil do colaborador (consulta por admin), incluindo experiências concluídas (máx. 2).
 */
@RestController
@RequestMapping(value = "/api/admin/employees", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdminEmployeeProfileController {

    private final AdminEmployeeProfileService adminEmployeeProfileService;

    @GetMapping("/{id}/profile")
    public AdminEmployeeProfileResponse getProfile(@PathVariable Integer id) {
        return adminEmployeeProfileService.getProfile(id);
    }
}
