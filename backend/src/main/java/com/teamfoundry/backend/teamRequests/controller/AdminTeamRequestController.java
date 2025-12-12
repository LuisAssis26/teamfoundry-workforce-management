package com.teamfoundry.backend.teamRequests.controller;

import com.teamfoundry.backend.teamRequests.dto.teamRequest.AssignedTeamRequestResponse;
import com.teamfoundry.backend.teamRequests.dto.teamRequest.TeamRequestRoleSummary;
import com.teamfoundry.backend.teamRequests.dto.teamRequest.TeamRequestResponse;
import com.teamfoundry.backend.teamRequests.service.TeamRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/work-requests")
@RequiredArgsConstructor
public class AdminTeamRequestController {

    private final TeamRequestService teamRequestService;

    @GetMapping
    public List<AssignedTeamRequestResponse> listAssignedRequests() {
        return teamRequestService.listAssignedRequestsForAuthenticatedAdmin();
    }

    @GetMapping("/{id}")
    public TeamRequestResponse getAssignedRequest(@PathVariable int id) {
        return teamRequestService.getAssignedRequest(id);
    }

    @GetMapping("/{id}/roles")
    public List<TeamRequestRoleSummary> listRoleRequests(@PathVariable int id) {
        return teamRequestService.listRoleSummariesForTeam(id);
    }
}
