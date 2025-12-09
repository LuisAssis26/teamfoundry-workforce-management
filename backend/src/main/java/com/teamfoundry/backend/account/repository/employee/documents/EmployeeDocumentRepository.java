package com.teamfoundry.backend.account.repository.employee.documents;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.enums.DocumentType;
import com.teamfoundry.backend.account.model.employee.documents.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Integer> {
    Optional<EmployeeDocument> findByEmployeeAndType(EmployeeAccount employee, DocumentType type);

    List<EmployeeDocument> findAllByEmployee(EmployeeAccount employee);
}
