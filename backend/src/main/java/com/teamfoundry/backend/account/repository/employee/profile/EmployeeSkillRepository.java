package com.teamfoundry.backend.account.repository.employee.profile;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Integer> {
    void deleteByEmployee(EmployeeAccount employee);

    List<EmployeeSkill> findByEmployee(EmployeeAccount employee);
}
