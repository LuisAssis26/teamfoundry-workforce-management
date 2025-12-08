package com.teamfoundry.backend.account.repository;

import com.teamfoundry.backend.account.model.EmployeeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeAccountRepository extends JpaRepository<EmployeeAccount, Integer> {
    Optional<EmployeeAccount> findByEmail(String email);

    @Query("""
            SELECT DISTINCT e
            FROM EmployeeAccount e
            LEFT JOIN com.teamfoundry.backend.account_options.model.employee.EmployeeFunction ef ON ef.employee = e
            LEFT JOIN ef.function f
            LEFT JOIN com.teamfoundry.backend.account_options.model.employee.EmployeeGeoArea ega ON ega.employee = e
            LEFT JOIN ega.geoArea ga
            LEFT JOIN com.teamfoundry.backend.account_options.model.employee.EmployeeCompetence ec ON ec.employee = e
            LEFT JOIN ec.competence c
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
