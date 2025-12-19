package com.teamfoundry.backend.teamRequests.service;

import com.teamfoundry.backend.teamRequests.dto.search.EmployeeJobSummary;
import com.teamfoundry.backend.teamRequests.enums.State;
import com.teamfoundry.backend.teamRequests.model.EmployeeRequest;
import com.teamfoundry.backend.teamRequests.model.EmployeeRequestOffer;
import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestOfferRepository;
import com.teamfoundry.backend.teamRequests.repository.EmployeeRequestRepository;
import com.teamfoundry.backend.account.model.company.CompanyAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.common.service.ActionLogService;
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
import java.util.Objects;

/**
 * Ofertas e historico do colaborador.
 * - listInvitedOffers: convites (ativos/fechados) + aceites do proprio, com status OPEN/ACCEPTED/CLOSED.
 * - acceptOffer: valida convite, evita dupla alocacao na mesma equipa e inativa convites da vaga.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeJobHistoryService {

    private final EmployeeRequestRepository employeeRequestRepository;
    private final EmployeeAccountRepository employeeAccountRepository;
    private final EmployeeRequestOfferRepository employeeRequestOfferRepository;
    private final com.teamfoundry.backend.teamRequests.repository.TeamRequestRepository teamRequestRepository;
    private final com.teamfoundry.backend.notification.service.NotificationService notificationService;
    private final ActionLogService actionLogService;

    @Transactional(readOnly = true)
    public List<EmployeeJobSummary> listJobsForEmployee(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador nao autenticado.");
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador nao autenticado.");
        }
        String normalized = email.trim().toLowerCase();
        EmployeeAccount employee = findEmployee(normalized);

        List<EmployeeRequestOffer> invites = employeeRequestOfferRepository.findAllInvitesByEmployeeEmail(normalized);
        List<EmployeeRequest> acceptedByUser = employeeRequestRepository.findByEmployee_EmailOrderByAcceptedDateDesc(normalized);

        Map<String, EmployeeJobSummary> groupedSummaries = new LinkedHashMap<>();


        for (EmployeeRequestOffer invite : invites) {
            EmployeeRequest req = invite.getEmployeeRequest();
            if (req == null || req.getTeamRequest() == null) continue;

            TeamRequest teamRequest = req.getTeamRequest();
            Integer teamId = teamRequest.getId();
            String role = req.getRequestedRole();
            String key = teamId + "::" + role;

            boolean isPersonalAccept = req.getEmployee() != null && Objects.equals(req.getEmployee().getId(), employee.getId());
            

            if (isPersonalAccept) {
                groupedSummaries.put("ACCEPTED_" + req.getId(), toSummary(req, "ACCEPTED"));
                continue;
            }


            boolean slotIsOpen = (req.getEmployee() == null) && !isConcluded(teamRequest);
            
            // Logica de agrupamento
            // Logica de agrupamento
            groupedSummaries.compute(key, (k, existing) -> {
                if (existing == null) {

                    String status = slotIsOpen ? "OPEN" : "CLOSED";
                    return toSummary(req, status);
                } else {

                    if ("CLOSED".equals(existing.getStatus()) && slotIsOpen) {
                        return toSummary(req, "OPEN");

                    }
                    return existing;
                }
            });
        }


        for (EmployeeRequest req : acceptedByUser) {
            if (req.getTeamRequest() == null) continue;

            groupedSummaries.put("ACCEPTED_" + req.getId(), toSummary(req, "ACCEPTED"));
            

            Integer teamId = req.getTeamRequest().getId();
            String role = req.getRequestedRole();
            String key = teamId + "::" + role;
            groupedSummaries.remove(key);
        }

        return groupedSummaries.values().stream().toList();
    }

    public EmployeeJobSummary acceptOffer(Integer requestId, String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador nao autenticado.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        EmployeeAccount employee = findEmployee(normalizedEmail);


        EmployeeRequest initialRequest = employeeRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta nao encontrada."));

        final int targetTeamId = initialRequest.getTeamRequest().getId();
        final String targetRole = initialRequest.getRequestedRole();


        boolean invited = employeeRequestOfferRepository
                .findActiveInvitesByEmployeeEmail(normalizedEmail)
                .stream()
                .anyMatch(inv -> inv.getEmployeeRequest() != null &&
                        inv.getEmployeeRequest().getTeamRequest().getId() == targetTeamId &&
                        inv.getEmployeeRequest().getRequestedRole().equals(targetRole));

        if (!invited) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem convite para esta oferta.");
        }

        EmployeeRequest targetRequest = initialRequest;

        if (targetRequest.getEmployee() != null) {
            // Find another open slot in the same Team Request for the same Role
            List<EmployeeRequest> siblings = employeeRequestRepository.findByTeamRequest_IdAndRequestedRole(
                    targetRequest.getTeamRequest().getId(),
                    targetRequest.getRequestedRole()
            );

            targetRequest = siblings.stream()
                    .filter(r -> r.getEmployee() == null)
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Vagas esgotadas."));
        }



        if (targetRequest.getEmployee() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Todas as vagas para esta função foram preenchidas.");
        }

        TeamRequest teamRequest = targetRequest.getTeamRequest();
        Integer teamId = teamRequest != null ? teamRequest.getId() : null;
        

        if (teamId != null && employeeRequestRepository.countAcceptedForTeam(teamId, employee.getId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voce ja ocupa uma vaga nesta equipa.");
        }


        LocalDateTime startDate = teamRequest != null ? teamRequest.getStartDate() : null;
        LocalDateTime endDate = teamRequest != null ? teamRequest.getEndDate() : null;
        if (startDate != null && endDate != null) {
            long overlapping = employeeRequestRepository.countOverlappingAccepted(
                    employee.getId(),
                    targetRequest.getId(),
                    startDate,
                    endDate
            );
            if (overlapping > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Voce ja tem um trabalho que colide nessas datas.");
            }
        }


        targetRequest.setEmployee(employee);
        targetRequest.setAcceptedDate(LocalDateTime.now());
        EmployeeRequest saved = employeeRequestRepository.save(targetRequest);


        List<EmployeeRequestOffer> userInvites = employeeRequestOfferRepository.findActiveInvitesByEmployeeEmail(normalizedEmail);
        for (EmployeeRequestOffer offer : userInvites) {
            if (offer.getEmployeeRequest().getTeamRequest().getId() == teamId && 
                offer.getEmployeeRequest().getRequestedRole().equals(targetRequest.getRequestedRole())) {
                offer.setActive(false);
                employeeRequestOfferRepository.save(offer);
            }
        }

        actionLogService.logUser(employee, "Aceitou oferta " + saved.getId() + " (Pool)");


        if (teamId != null) {
            long openSpots = employeeRequestRepository.countByTeamRequest_IdAndEmployeeIsNull(teamId);
            if (openSpots == 0) {
                teamRequest.setState(State.COMPLETE);
                teamRequestRepository.save(teamRequest);
                
                if (teamRequest.getCompany() != null) {
                    notificationService.createNotification(
                        teamRequest.getCompany(),
                        "Sua requisição de equipa '" + teamRequest.getTeamName() + "' está completa!",
                        com.teamfoundry.backend.notification.enums.NotificationType.REQUEST_COMPLETED,
                        teamRequest.getId()
                    );
                }
            }
        }

        return toSummary(saved, "ACCEPTED");
    }

    @Transactional(readOnly = true)
    public long countOpenInvites(String email) {
        return listInvitedOffers(email).stream()
                .filter(summary -> "OPEN".equalsIgnoreCase(summary.getStatus()))
                .count();
    }

    @Transactional(readOnly = true)
    public String findCurrentCompanyName(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilizador nao autenticado.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        EmployeeAccount employee = findEmployee(normalizedEmail);

        return employeeRequestRepository.findByEmployee_IdOrderByAcceptedDateDesc(employee.getId())
                .stream()
                .findFirst()
                .map(request -> {
                    TeamRequest teamRequest = request.getTeamRequest();
                    CompanyAccount company = teamRequest != null ? teamRequest.getCompany() : null;
                    return company != null ? company.getName() : null;
                })
                .orElse(null);
    }

    private EmployeeAccount findEmployee(String normalizedEmail) {
        return employeeAccountRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta nao encontrada."));
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
