package com.teamfoundry.backend.account.model.employee.profile;

import com.teamfoundry.backend.account.model.preferences.PrefRole;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_role")
public class EmployeeRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_funcionario", nullable = false)
    private EmployeeAccount employee;

    @ManyToOne
    @JoinColumn(name = "id_funcao", nullable = false)
    private PrefRole function;
}
