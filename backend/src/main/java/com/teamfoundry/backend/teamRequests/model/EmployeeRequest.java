package com.teamfoundry.backend.teamRequests.model;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "request_employee")
public class EmployeeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_team_request", nullable = false)
    private TeamRequest teamRequest;

    @ManyToOne
    @JoinColumn(name = "id_funcionario")
    private EmployeeAccount employee;

    @Column(name = "requested_role", nullable = false)
    private String requestedRole;

    @Column(name = "salary")
    private BigDecimal requestedSalary;

    @Column(name = "date_accepted")
    private LocalDateTime acceptedDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
