package com.teamfoundry.backend.account.controller.employee.documents;

import com.teamfoundry.backend.account.dto.employee.documents.EmployeeCertificationRequest;
import com.teamfoundry.backend.account.dto.employee.documents.EmployeeCertificationResponse;
import com.teamfoundry.backend.account.service.employee.EmployeeCertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/employee/certifications", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EmployeeCertificationController {

    private final EmployeeCertificationService employeeCertificationService;

    @GetMapping
    public List<EmployeeCertificationResponse> list(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return employeeCertificationService.list(email);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeCertificationResponse create(@Valid @RequestBody EmployeeCertificationRequest request,
                                                Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return employeeCertificationService.create(email, request);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EmployeeCertificationResponse update(@PathVariable int id,
                                                @Valid @RequestBody EmployeeCertificationRequest request,
                                                Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return employeeCertificationService.update(id, email, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        employeeCertificationService.delete(id, email);
    }
}
