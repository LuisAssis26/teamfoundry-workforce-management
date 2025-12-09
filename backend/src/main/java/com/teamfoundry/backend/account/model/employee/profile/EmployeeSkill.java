package com.teamfoundry.backend.account.model.employee.profile;

import com.teamfoundry.backend.account.model.preferences.PrefSkill;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_skill")
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_funcionario", nullable = false)
    private EmployeeAccount employee;

    @ManyToOne
    @JoinColumn(name = "id_competencia", nullable = false)
    private PrefSkill prefSkill;
}
