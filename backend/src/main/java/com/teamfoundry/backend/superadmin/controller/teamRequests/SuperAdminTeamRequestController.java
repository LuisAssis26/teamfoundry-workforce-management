package com.teamfoundry.backend.superadmin.controller.teamRequests;

import com.teamfoundry.backend.teamRequests.dto.AssignedAdminTeamRequestCount;
import com.teamfoundry.backend.teamRequests.dto.teamRequest.TeamRequestResponse;
import com.teamfoundry.backend.teamRequests.service.TeamRequestService;
import com.teamfoundry.backend.superadmin.dto.teamRequests.AssignAdminRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/work-requests")
@RequiredArgsConstructor
public class SuperAdminTeamRequestController {

    private final TeamRequestService teamRequestService;

    @GetMapping
    public List<TeamRequestResponse> listAll() {
        return teamRequestService.listAllWorkRequests();
    }

    @GetMapping("/admin-options")
    public List<AssignedAdminTeamRequestCount> listAdminOptions() {
        return teamRequestService.listAssignableAdmins();
    }

    @PatchMapping("/{id}/responsible-admin")
    public TeamRequestResponse assignResponsibleAdmin(@PathVariable int id,
                                                      @Valid @RequestBody AssignAdminRequest request) {
        return teamRequestService.assignResponsibleAdmin(id, request.adminId());
    }
}
