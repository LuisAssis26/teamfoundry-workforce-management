package com.teamfoundry.backend.account.model.employee.documents;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.enums.DocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_document",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "doc_type"}))
@Getter
@Setter
@NoArgsConstructor
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private EmployeeAccount employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 32)
    private DocumentType type;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "file_name")
    private String fileName;
}
