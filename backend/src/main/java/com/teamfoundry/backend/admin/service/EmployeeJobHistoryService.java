package com.teamfoundry.backend.admin.service;

import com.teamfoundry.backend.admin.dto.EmployeeJobSummary;
import com.teamfoundry.backend.admin.enums.State;
import com.teamfoundry.backend.admin.model.EmployeeRequest;
import com.teamfoundry.backend.admin.model.EmployeeRequestEmployee;
import com.teamfoundry.backend.admin.model.TeamRequest;
import com.teamfoundry.backend.admin.repository.EmployeeRequestEmployeeRepository;
import com.teamfoundry.backend.admin.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.account.model.CompanyAccount;
import com.teamfoundry.backend.account.model.EmployeeAccount;
import com.teamfoundry.backend.account.repository.EmployeeAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ofertas e histórico do colaborador.
 * - listInvitedOffers: convites (ativos/fechados) + aceites do próprio, com status OPEN/ACCEPTED/CLOSED.
 * - acceptOffer: valida convite, evita dupla alocação na mesma equipa e inativa convites da vaga.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeJobHistoryService {

    private final EmployeeRequestRepository employeeRequestRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final EmployeeRequestEmployeeRepository employeeRequestEmployeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeJobSummary> listJobsForEmployee(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador não autenticado.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        List<EmployeeRequest> requests = employeeRequestRepository.findByEmployee_EmailOrderByAcceptedDateDesc(normalizedEmail);
        return requests.stream()
                .map(req -> toSummary(req, "ACCEPTED"))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeJobSummary> listInvitedOffers(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador não autenticado.");
        }
        String normalized = email.trim().toLowerCase();
        EmployeeAccount employee = findEmployee(normalized);

        List<EmployeeRequestEmployee> invites = employeeRequestEmployeeRepository.findAllInvitesByEmployeeEmail(normalized);
        List<EmployeeRequest> acceptedByUser = employeeRequestRepository.findByEmployee_EmailOrderByAcceptedDateDesc(normalized);

        Map<Integer, EmployeeJobSummary> summaries = new LinkedHashMap<>();

        // Convites (inclui fechados/aceites)
        for (EmployeeRequestEmployee invite : invites) {
            EmployeeRequest req = invite.getEmployeeRequest();
            if (req == null || req.getTeamRequest() == null) continue;

            String status = "OPEN";
            if (req.getEmployee() != null) {
                status = req.getEmployee().getId() == employee.getId() ? "ACCEPTED" : "CLOSED";
            } else if (isConcluded(req.getTeamRequest())) {
                status = "CLOSED";
            }
            summaries.put(req.getId(), toSummary(req, status));
        }

        // Garantir que aceites do próprio sempre aparecem
        for (EmployeeRequest req : acceptedByUser) {
            if (req.getTeamRequest() == null) continue;
            summaries.put(req.getId(), toSummary(req, "ACCEPTED"));
        }

        return summaries.values().stream().toList();
    }

    public EmployeeJobSummary acceptOffer(Integer requestId, String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador não autenticado.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        EmployeeAccount employee = findEmployee(normalizedEmail);

        EmployeeRequest request = employeeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta não encontrada."));

        if (request.getEmployee() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vaga já ocupada.");
        }

        TeamRequest teamRequest = request.getTeamRequest();
        Integer teamId = teamRequest != null ? teamRequest.getId() : null;
        if (teamId != null && employeeRequestRepository.countAcceptedForTeam(teamId, employee.getId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já ocupa uma vaga nesta equipa.");
        }

        LocalDateTime startDate = teamRequest != null ? teamRequest.getStartDate() : null;
        LocalDateTime endDate = teamRequest != null ? teamRequest.getEndDate() : null;
        if (startDate != null && endDate != null) {
            long overlapping = employeeRequestRepository.countOverlappingAccepted(
                    employee.getId(),
                    request.getId(),
                    startDate,
                    endDate
            );
            if (overlapping > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Você já tem um trabalho que colide nessas datas.");
            }
        }

        boolean invited = employeeRequestEmployeeRepository
                .findActiveInvitesByEmployeeEmail(normalizedEmail)
                .stream()
                .anyMatch(inv -> inv.getEmployeeRequest() != null && inv.getEmployeeRequest().getId() == requestId);
        if (!invited) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem convite para esta oferta.");
        }

        request.setEmployee(employee);
        request.setAcceptedDate(LocalDateTime.now());
        EmployeeRequest saved = employeeRequestRepository.save(request);

        employeeRequestEmployeeRepository.deactivateInvitesForRequest(requestId, employee.getId());

        return toSummary(saved, "ACCEPTED");
    }


    private EmployeeAccount findEmployee(String normalizedEmail) {
        return employeeAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta não encontrada."));
    }

    private EmployeeJobSummary toSummary(EmployeeRequest req, String status) {
        TeamRequest teamRequest = req.getTeamRequest();
        CompanyAccount company = teamRequest != null ? teamRequest.getCompany() : null;

        return EmployeeJobSummary.builder()
                .requestId(req.getId())
                .teamName(teamRequest != null ? teamRequest.getTeamName() : null)
                .companyName(company != null ? company.getName() : null)
                .location(teamRequest != null ? teamRequest.getLocation() : null)
                .description(teamRequest != null ? teamRequest.getDescription() : null)
                .startDate(teamRequest != null ? teamRequest.getStartDate() : null)
                .endDate(teamRequest != null ? teamRequest.getEndDate() : null)
                .acceptedDate(req.getAcceptedDate())
                .requestedRole(req.getRequestedRole())
                .status(status)
                .build();
    }

    private boolean isConcluded(TeamRequest teamRequest) {
        if (teamRequest == null) return false;
        if (teamRequest.getState() == State.COMPLETE) return true;
        LocalDateTime end = teamRequest.getEndDate();
        return end != null && end.isBefore(LocalDateTime.now());
    }
}
