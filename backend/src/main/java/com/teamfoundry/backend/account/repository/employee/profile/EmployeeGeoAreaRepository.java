package com.teamfoundry.backend.account.repository.employee.profile;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeGeoArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeGeoAreaRepository extends JpaRepository<EmployeeGeoArea, Integer> {
    void deleteByEmployee(EmployeeAccount employee);

    List<EmployeeGeoArea> findByEmployee(EmployeeAccount employee);
}
