package com.teamfoundry.backend.teamRequests.service;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import com.teamfoundry.backend.teamRequests.dto.teamRequest.AssignedTeamRequestResponse;
import com.teamfoundry.backend.teamRequests.dto.teamRequest.TeamRequestRoleSummary;
import com.teamfoundry.backend.teamRequests.dto.AssignedAdminTeamRequestCount;
import com.teamfoundry.backend.teamRequests.dto.teamRequest.TeamRequestResponse;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestOfferRepository;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.stream.Collectors;


import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRequestService {

    private static final String ADMIN_TOKEN_PREFIX = "admin:";

    private final TeamRequestRepository teamRequestRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final EmployeeRequestRepository employeeRequestRepository;
    private final EmployeeRequestOfferRepository employeeRequestOfferRepository;

    public List<TeamRequestResponse> listAllWorkRequests() {
        return teamRequestRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toWorkRequestResponse)
                .toList();
    }

    public List<AssignedAdminTeamRequestCount> listAssignableAdmins() {
        Map<Integer, Long> counts = teamRequestRepository.countAssignmentsGroupedByAdmin()
                .stream()
                .filter(row -> row.getAdminId() != null)
                .collect(Collectors.toMap(
                        TeamRequestRepository.AdminAssignmentCount::getAdminId,
                        TeamRequestRepository.AdminAssignmentCount::getTotal));

        return adminAccountRepository
                .findAll(Sort.by(Sort.Direction.ASC, "username"))
                .stream()
                .filter(admin -> admin.getRole() == UserType.ADMIN)
                .map(admin -> new AssignedAdminTeamRequestCount(
                        admin.getId(),
                        admin.getUsername(),
                        counts.getOrDefault(admin.getId(), 0L)))
                .toList();
    }

    @Transactional
    public TeamRequestResponse assignResponsibleAdmin(int requestId, int adminId) {
        TeamRequest request = teamRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));

        AdminAccount admin = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado."));

        if (admin.getRole() != UserType.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Somente administradores comuns podem ser responsáveis.");
        }

        request.setResponsibleAdminId(admin.getId());
        TeamRequest saved = teamRequestRepository.save(request);
        return toWorkRequestResponse(saved);
    }

    public List<AssignedTeamRequestResponse> listAssignedRequestsForAuthenticatedAdmin() {
        AdminAccount admin = resolveAuthenticatedAdmin();
        List<TeamRequest> requests = teamRequestRepository.findByResponsibleAdminId(admin.getId());
        Map<Integer, Long> workforceByRequest = loadWorkforceCounts(requests);

        return requests.stream()
                .map(request -> toAssignedResponse(request, workforceByRequest.getOrDefault(request.getId(), 0L)))
                .toList();
    }

    public TeamRequestResponse getAssignedRequest(int requestId) {
        AdminAccount admin = resolveAuthenticatedAdmin();
        TeamRequest request = loadRequestForAdmin(admin, requestId);
        return toWorkRequestResponse(request);
    }

    public List<TeamRequestRoleSummary> listRoleSummariesForTeam(int requestId) {
        AdminAccount admin = resolveAuthenticatedAdmin();
        TeamRequest request = loadRequestForAdmin(admin, requestId);

        Map<String, RoleAggregate> aggregates = new LinkedHashMap<>();
        employeeRequestRepository.countByRoleForTeam(request.getId()).forEach(row -> {
            aggregates.put(row.getRole(), new RoleAggregate(row.getRole(), row.getTotal(), row.getFilled(), 0L));
        });

        employeeRequestOfferRepository.countInvitesByTeamRequest(request.getId()).forEach(row -> {
            RoleAggregate agg = aggregates.computeIfAbsent(row.getRole(),
                    role -> new RoleAggregate(role, 0L, 0L, 0L));
            agg.setProposalsSent(row.getTotal());
        });

        return aggregates.values().stream()
                .map(agg -> new TeamRequestRoleSummary(
                        agg.role(),
                        agg.totalRequested(),
                        agg.filled(),
                        Math.max(agg.totalRequested() - agg.filled(), 0),
                        agg.proposalsSent()
                ))
                .toList();
    }

    private Map<Integer, Long> loadWorkforceCounts(List<TeamRequest> requests) {
        Set<Integer> ids = requests.stream()
                .map(TeamRequest::getId)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        return employeeRequestRepository.countByTeamRequestIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        EmployeeRequestRepository.TeamRequestCount::getRequestId,
                        EmployeeRequestRepository.TeamRequestCount::getTotal));
    }

    private TeamRequest loadRequestForAdmin(AdminAccount admin, int requestId) {
        TeamRequest request = teamRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisição não encontrada."));
        if (!Objects.equals(request.getResponsibleAdminId(), admin.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requisição não atribuída a este administrador.");
        }
        return request;
    }

    private TeamRequestResponse toWorkRequestResponse(TeamRequest request) {
        CompanyAccount company = request.getCompany();
        String companyName = company != null ? company.getName() : null;
        String companyEmail = company != null ? company.getEmail() : null;

        return new TeamRequestResponse(
                request.getId(),
                companyName,
                companyEmail,
                request.getTeamName(),
                request.getDescription(),
                request.getState(),
                request.getResponsibleAdminId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getCreatedAt()
        );
    }

    private AssignedTeamRequestResponse toAssignedResponse(TeamRequest request, long workforceNeeded) {
        CompanyAccount company = request.getCompany();
        return new AssignedTeamRequestResponse(
                request.getId(),
                company != null ? company.getName() : null,
                company != null ? company.getEmail() : null,
                company != null ? company.getPhone() : null,
                workforceNeeded,
                request.getState(),
                request.getCreatedAt()
        );
    }

    private AdminAccount resolveAuthenticatedAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação requerida.");
        }

        String principal = authentication.getName();
        if (principal == null || !principal.startsWith(ADMIN_TOKEN_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente administradores autenticados.");
        }

        String username = principal.substring(ADMIN_TOKEN_PREFIX.length());
        return adminAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrador não encontrado."));
    }

    private static class RoleAggregate {
        private final String role;
        private final long totalRequested;
        private final long filled;
        private long proposalsSent;

        RoleAggregate(String role, long totalRequested, long filled, long proposalsSent) {
            this.role = role;
            this.totalRequested = totalRequested;
            this.filled = filled;
            this.proposalsSent = proposalsSent;
        }

        String role() {
            return role;
        }

        long totalRequested() {
            return totalRequested;
        }

        long filled() {
            return filled;
        }

        long proposalsSent() {
            return proposalsSent;
        }

        void setProposalsSent(long proposalsSent) {
            this.proposalsSent = proposalsSent;
        }
    }
}
