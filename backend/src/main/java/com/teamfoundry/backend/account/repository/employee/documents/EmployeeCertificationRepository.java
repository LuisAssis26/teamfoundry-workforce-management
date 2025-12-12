package com.teamfoundry.backend.account.repository.employee.documents;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.model.employee.documents.EmployeeCertification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeCertificationRepository extends JpaRepository<EmployeeCertification, Integer> {

    List<EmployeeCertification> findByEmployeeOrderByCompletionDateDescIdDesc(EmployeeAccount employee);

    Optional<EmployeeCertification> findByIdAndEmployee(Integer id, EmployeeAccount employee);

    Optional<EmployeeCertification> findByEmployeeAndNameIgnoreCaseAndInstitutionIgnoreCaseAndCompletionDate(
            EmployeeAccount employee,
            String name,
            String institution,
            LocalDate completionDate
    );
}
