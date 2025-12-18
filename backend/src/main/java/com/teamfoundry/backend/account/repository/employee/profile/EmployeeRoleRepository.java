package com.teamfoundry.backend.account.repository.employee.profile;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRoleRepository extends JpaRepository<EmployeeRole, Integer> {
    void deleteByEmployee(EmployeeAccount employee);

    Optional<EmployeeRole> findFirstByEmployee(EmployeeAccount employee);

    java.util.List<EmployeeRole> findByEmployee(EmployeeAccount employee);
}
