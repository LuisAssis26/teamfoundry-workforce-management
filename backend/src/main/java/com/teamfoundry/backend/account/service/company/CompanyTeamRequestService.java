package com.teamfoundry.backend.account.service.company;

import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.repository.company.CompanyAccountRepository;
import com.teamfoundry.backend.account.dto.company.teamRequests.CompanyTeamRequestCreateRequest;
import com.teamfoundry.backend.account.dto.company.teamRequests.CompanyTeamRequestResponse;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.account.enums.UserType;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import com.teamfoundry.backend.teamRequests.model.EmployeeRequest;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository;
import com.teamfoundry.backend.superadmin.repository.credentials.AdminAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para consultar e criar requisições de equipa pela empresa autenticada.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyTeamRequestService {

    private final TeamRequestRepository teamRequestRepository;
    private final EmployeeRequestRepository employeeRequestRepository;
    private final CompanyAccountRepository companyAccountRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final com.teamfoundry.backend.common.service.ActionLogService actionLogService;

    /**
     * Lista todas as requisições da empresa autenticada, ordenadas por criação.
     */
    public List<CompanyTeamRequestResponse> listCompanyRequests(String email) {
        CompanyAccount company = resolveCompany(email);
        return teamRequestRepository.findByCompany_EmailOrderByCreatedAtDesc(company.getEmail())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Cria uma nova requisição de equipa para a empresa autenticada.
     */
    @Transactional
    public CompanyTeamRequestResponse createRequest(String email, CompanyTeamRequestCreateRequest request) {
        CompanyAccount company = resolveCompany(email);
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adicione pelo menos uma função.");
        }
        TeamRequest entity = new TeamRequest();
        entity.setCompany(company);
        entity.setTeamName(request.getTeamName());
        entity.setDescription(request.getDescription());
        entity.setLocation(request.getLocation());
        entity.setState(State.INCOMPLETE);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setResponsibleAdminId(resolveLeastLoadedAdmin());

        TeamRequest saved = teamRequestRepository.save(entity);
        createEmployeeRequests(saved, request);
        actionLogService.logUser(company, "Criou requisição de equipa " + saved.getTeamName());
        return toResponse(saved);
    }

    private void createEmployeeRequests(TeamRequest teamRequest, CompanyTeamRequestCreateRequest request) {
        List<EmployeeRequest> requests = request.getRoles().stream().flatMap(roleReq -> {
            return java.util.stream.IntStream.range(0, roleReq.getQuantity())
                    .mapToObj(i -> {
                        EmployeeRequest er = new EmployeeRequest();
                        er.setTeamRequest(teamRequest);
                        er.setRequestedRole(roleReq.getRole());
                        er.setRequestedSalary(roleReq.getSalary());
                        er.setCreatedAt(LocalDateTime.now());
                        return er;
                    });
        }).toList();
        if (!requests.isEmpty()) {
            employeeRequestRepository.saveAll(requests);
        }
    }

    private CompanyTeamRequestResponse toResponse(TeamRequest request) {
        return CompanyTeamRequestResponse.builder()
                .id(request.getId())
                .teamName(request.getTeamName())
                .description(request.getDescription())
                .location(request.getLocation())
                .state(request.getState())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(request.getCreatedAt())
                .computedStatus(deriveStatus(request))
                .build();
    }

    /**
     * Calcula rótulo amigável para as tabs (ativa/pendente/passada).
     */
    private String deriveStatus(TeamRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if (request.getState() == State.COMPLETED || (request.getEndDate() != null && request.getEndDate().isBefore(now))) {
            return "PAST";
        }
        if (request.getStartDate() != null && request.getStartDate().isAfter(now)) {
            return "PENDING";
        }
        return "ACTIVE";
    }

    private CompanyAccount resolveCompany(String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autenticação requerida.");
        }
        return companyAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada."));
    }

    /**
     * Escolhe o admin (role ADMIN) com menos team requests atribuídas.
     * Em caso de empate, retorna o de menor id. Caso não haja admins, devolve null.
     */
    private Integer resolveLeastLoadedAdmin() {
        // Contagem atual por admin
        var counts = teamRequestRepository.countAssignmentsGroupedByAdmin();
        java.util.Map<Integer, Long> countMap = new java.util.HashMap<>();
        for (var row : counts) {
            if (row.getAdminId() != null) {
                countMap.put(row.getAdminId(), row.getTotal());
            }
        }

        return adminAccountRepository.findAll()
                .stream()
                .filter(admin -> admin.getRole() == UserType.ADMIN)
                .min((a, b) -> {
                    long countA = countMap.getOrDefault(a.getId(), 0L);
                    long countB = countMap.getOrDefault(b.getId(), 0L);
                    if (countA == countB) {
                        return Integer.compare(a.getId(), b.getId());
                    }
                    return Long.compare(countA, countB);
                })
                .map(AdminAccount::getId)
                .orElse(null);
    }
}
