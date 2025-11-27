package com.teamfoundry.backend.admin.service;

import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.account.model.AdminAccount;
import com.teamfoundry.backend.account.model.CompanyAccount;
import com.teamfoundry.backend.account.repository.AdminAccountRepository;
import com.teamfoundry.backend.admin.dto.AssignedTeamRequestResponse;
import com.teamfoundry.backend.admin.dto.WorkRequestAdminOption;
import com.teamfoundry.backend.admin.dto.WorkRequestResponse;
import com.teamfoundry.backend.admin.model.TeamRequest;
import com.teamfoundry.backend.admin.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.admin.repository.TeamRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamRequestService {

    private static final String ADMIN_TOKEN_PREFIX = "admin:";

    private final TeamRequestRepository teamRequestRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final EmployeeRequestRepository employeeRequestRepository;

    public List<WorkRequestResponse> listAllWorkRequests() {
        return teamRequestRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toWorkRequestResponse)
                .toList();
    }

    public List<WorkRequestAdminOption> listAssignableAdmins() {
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
                .map(admin -> new WorkRequestAdminOption(
                        admin.getId(),
                        admin.getUsername(),
                        counts.getOrDefault(admin.getId(), 0L)))
                .toList();
    }

    @Transactional
    public WorkRequestResponse assignResponsibleAdmin(int requestId, int adminId) {
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

    private WorkRequestResponse toWorkRequestResponse(TeamRequest request) {
        CompanyAccount company = request.getCompany();
        String companyName = company != null ? company.getName() : null;
        String companyEmail = company != null ? company.getEmail() : null;

        return new WorkRequestResponse(
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


    // atribuir automaticamente o admin com menos requisições ativas - utilizar no forms de request de time
    @Transactional
    public TeamRequest createTeamRequestWithAutoAssign(TeamRequest request) {
        Integer adminId = pickLeastLoadedAdminId();
        request.setResponsibleAdminId(adminId); // fica null se não houver admins, superadmin pode atribuir depois
        return teamRequestRepository.save(request);
    }

    private Integer pickLeastLoadedAdminId() {
        // mapa adminId -> total atribuições
        Map<Integer, Long> counts = teamRequestRepository.countAssignmentsGroupedByAdmin()
                .stream()
                .filter(row -> row.getAdminId() != null)
                .collect(Collectors.toMap(
                        TeamRequestRepository.AdminAssignmentCount::getAdminId,
                        TeamRequestRepository.AdminAssignmentCount::getTotal));

        // lista de admins elegíveis (role ADMIN)
        List<AdminAccount> admins = adminAccountRepository.findAll(Sort.by(Sort.Direction.ASC, "username"))
                .stream()
                .filter(a -> a.getRole() == UserType.ADMIN)
                .toList();

        if (admins.isEmpty()) return null;

        // escolhe quem tem menos requisições; desempate por id
        return admins.stream()
                .min((a, b) -> {
                    long ca = counts.getOrDefault(a.getId(), 0L);
                    long cb = counts.getOrDefault(b.getId(), 0L);
                    int diff = Long.compare(ca, cb);
                    return diff != 0 ? diff : Integer.compare(a.getId(), b.getId());
                })
                .map(AdminAccount::getId)
                .orElse(null);
    }


}
