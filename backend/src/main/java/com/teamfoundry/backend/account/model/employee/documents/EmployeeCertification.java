package com.teamfoundry.backend.account.model.employee.documents;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employee_certification")
public class EmployeeCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_funcionario", nullable = false)
    private EmployeeAccount employee;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String institution;

    @Column
    private String location;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column
    private String description;

    @Column(name = "certificate_public_id")
    private String certificatePublicId;

    @Column(name = "certificate_file_name")
    private String certificateFileName;
}
