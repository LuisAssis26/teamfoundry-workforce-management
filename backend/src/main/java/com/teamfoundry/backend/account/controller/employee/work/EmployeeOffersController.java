package com.teamfoundry.backend.account.controller.employee.work;

import com.teamfoundry.backend.teamRequests.dto.search.EmployeeJobSummary;
import com.teamfoundry.backend.teamRequests.service.EmployeeJobHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ofertas de trabalho para o colaborador.
 * - listOffers: convites (ativos/fechados) + aceites do próprio, com status OPEN/ACCEPTED/CLOSED.
 * - accept: aceita convite, evitando dupla alocação na mesma equipa e inativando convites da vaga.
 */
@RestController
@RequestMapping(value = "/api/employee/offers", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EmployeeOffersController {

    private final EmployeeJobHistoryService employeeJobHistoryService;

    @GetMapping
    public List<EmployeeJobSummary> listOffers(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return employeeJobHistoryService.listInvitedOffers(email);
    }

    @PostMapping("/{id}/accept")
    public EmployeeJobSummary accept(@PathVariable("id") Integer requestId, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return employeeJobHistoryService.acceptOffer(requestId, email);
    }
}

