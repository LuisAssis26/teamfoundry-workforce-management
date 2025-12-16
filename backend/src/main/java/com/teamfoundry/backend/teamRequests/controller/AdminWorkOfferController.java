package com.teamfoundry.backend.teamRequests.controller;

import com.teamfoundry.backend.teamRequests.service.AdminWorkOfferService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Convites de colaboradores para vagas em equipas (admin).
 */
@RestController
@RequestMapping(value = "/api/admin/work-requests", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminWorkOfferController {

    private final AdminWorkOfferService adminWorkOfferService;

    public AdminWorkOfferController(AdminWorkOfferService adminWorkOfferService) {
        this.adminWorkOfferService = adminWorkOfferService;
    }

    @PostMapping("/{teamId}/roles/{role}/invites")
    public Map<String, Object> sendInvites(@PathVariable Integer teamId,
                                           @PathVariable String role,
                                           @RequestBody InviteRequest body) {
        int created = adminWorkOfferService.sendInvites(teamId, role, body.candidateIds());
        return Map.of("invitesCreated", created);
    }

    @GetMapping("/{teamId}/roles/{role}/invites")
    public List<Integer> listInvited(@PathVariable Integer teamId, @PathVariable String role) {
        return adminWorkOfferService.listActiveInviteIds(teamId, role);
    }

    @GetMapping("/{teamId}/invites")
    public List<Integer> listInvitedAnyRole(@PathVariable Integer teamId) {
        return adminWorkOfferService.listActiveInviteIds(teamId, null);
    }

    @GetMapping("/{teamId}/accepted")
    public List<Integer> listAccepted(@PathVariable Integer teamId) {
        return adminWorkOfferService.listAcceptedIds(teamId);
    }

    public record InviteRequest(@NotNull @NotEmpty List<Integer> candidateIds) {}
}
