package com.teamfoundry.backend.account.controller.employee.work;

import com.teamfoundry.backend.teamRequests.dto.search.EmployeeJobSummary;
import com.teamfoundry.backend.teamRequests.service.EmployeeJobHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/employee/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EmployeeJobsController {

    private final EmployeeJobHistoryService employeeJobHistoryService;

    /**
     * Devolve o histórico de participações do colaborador autenticado.
     */
    @GetMapping
    public List<EmployeeJobSummary> list(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return employeeJobHistoryService.listJobsForEmployee(email);
    }
}
