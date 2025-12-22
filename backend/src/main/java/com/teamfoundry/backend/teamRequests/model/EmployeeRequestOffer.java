package com.teamfoundry.backend.teamRequests.model;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "EmployeeRequestOffer")
@Table(name = "request_employee_offer")
public class EmployeeRequestOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_request", nullable = false)
    private EmployeeRequest employeeRequest;

    @ManyToOne
    @JoinColumn(name = "id_funcionario", nullable = false)
    private EmployeeAccount employee;

    @Column(nullable = false)
    private boolean active = true;
}
