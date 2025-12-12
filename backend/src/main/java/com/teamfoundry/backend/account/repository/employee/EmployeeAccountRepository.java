package com.teamfoundry.backend.account.repository.employee;

import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeAccountRepository extends JpaRepository<EmployeeAccount, Integer> {
    Optional<EmployeeAccount> findByEmail(String email);

    long countByDeactivatedFalse();

    @Query("""
            SELECT DISTINCT e
            FROM EmployeeAccount e
            LEFT JOIN com.teamfoundry.backend.account.model.employee.profile.EmployeeRole er ON er.employee = e
            LEFT JOIN er.function f
            LEFT JOIN com.teamfoundry.backend.account.model.employee.profile.EmployeeGeoArea ega ON ega.employee = e
            LEFT JOIN ega.geoArea ga
            LEFT JOIN com.teamfoundry.backend.account.model.employee.profile.EmployeeSkill es ON es.employee = e
            LEFT JOIN es.prefSkill c
            WHERE e.verified = true AND e.deactivated = false
              AND (:role IS NULL OR LOWER(f.name) = LOWER(:role))
              AND (:areasEmpty = true OR LOWER(ga.name) IN :areas)
              AND (:skillsEmpty = true OR LOWER(c.name) IN :skills)
            """)
    List<EmployeeAccount> searchCandidates(@Param("role") String role,
                                           @Param("areas") List<String> areas,
                                           @Param("areasEmpty") boolean areasEmpty,
                                           @Param("skills") List<String> skills,
                                           @Param("skillsEmpty") boolean skillsEmpty);
}
