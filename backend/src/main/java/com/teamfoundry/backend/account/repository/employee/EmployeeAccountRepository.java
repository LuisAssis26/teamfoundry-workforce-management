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

    @Query(value = """
            SELECT DISTINCT e.*, a.*
            FROM employee_account e
            JOIN account a ON e.id = a.id
            
            -- Function/Role Filters
            LEFT JOIN employee_role er ON er.id_funcionario = e.id
            LEFT JOIN pref_role pr ON pr.id = er.id_funcao
            
            -- Geo Filters
            LEFT JOIN employee_geo_area ega ON ega.id_funcionario = e.id
            LEFT JOIN pref_geo_areas pga ON pga.id = ega.id_area_geo
            
            -- Skill Filters
            LEFT JOIN employee_skill es ON es.id_funcionario = e.id
            LEFT JOIN pref_skill ps ON ps.id = es.id_competencia
            
            -- INVITE JOIN (Scoped to Team/Role)
            LEFT JOIN request_funcionario_oferta invite 
                ON invite.id_funcionario = e.id 
                AND invite.active = true
                AND invite.id_request IN (
                    SELECT sub_req.id FROM request_funcionario sub_req 
                    WHERE sub_req.id_team_request = :teamId 
                    AND (:vacancyRole IS NULL OR LOWER(sub_req.requested_role) = LOWER(:vacancyRole))
                )

            -- ASSIGNMENT JOIN (Scoped to Team/Role)
            LEFT JOIN request_funcionario assignment 
                ON assignment.id_funcionario = e.id 
                AND assignment.id_team_request = :teamId
                AND (:vacancyRole IS NULL OR LOWER(assignment.requested_role) = LOWER(:vacancyRole))

            WHERE a.verified = true AND a.deactivated = false
            
            -- Dynamic Filters
            AND (:rolesEmpty = true OR LOWER(pr.name) IN :roles)
            AND (:areasEmpty = true OR LOWER(pga.name) IN :areas)
            AND (:skillsEmpty = true OR LOWER(ps.name) IN :skills)
            
            -- STATUS FILTER LOGIC
            AND (:statusesEmpty = true OR (
                ('INVITED' IN :statuses AND invite.id IS NOT NULL) OR
                ('ACCEPTED' IN :statuses AND assignment.id IS NOT NULL) OR
                ('NO_PROPOSAL' IN :statuses AND invite.id IS NULL AND assignment.id IS NULL)
            ))
            """, nativeQuery = true)
    List<EmployeeAccount> searchCandidates(@Param("areas") List<String> areas,
                                           @Param("areasEmpty") boolean areasEmpty,
                                           @Param("skills") List<String> skills,
                                           @Param("skillsEmpty") boolean skillsEmpty,
                                           @Param("roles") List<String> roles,
                                           @Param("rolesEmpty") boolean rolesEmpty,
                                           @Param("statuses") List<String> statuses,
                                           @Param("statusesEmpty") boolean statusesEmpty,
                                           @Param("teamId") Integer teamId,
                                           @Param("vacancyRole") String vacancyRole);
}
