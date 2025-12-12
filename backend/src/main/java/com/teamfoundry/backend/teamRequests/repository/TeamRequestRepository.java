package com.teamfoundry.backend.teamRequests.repository;

import com.teamfoundry.backend.teamRequests.model.TeamRequest;
import com.teamfoundry.backend.teamRequests.enums.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para gerenciar TeamRequest (solicitações de equipe).
 * Inclui consultas por estado, administrador responsável e empresa.
 */
public interface TeamRequestRepository extends JpaRepository<TeamRequest, Integer> {

    List<TeamRequest> findByResponsibleAdminId(Integer adminId);

    @Query("""
            SELECT tr.responsibleAdminId AS adminId, COUNT(tr) AS total
            FROM TeamRequest tr
            WHERE tr.responsibleAdminId IS NOT NULL
            GROUP BY tr.responsibleAdminId
            """)
    List<AdminAssignmentCount> countAssignmentsGroupedByAdmin();

    List<TeamRequest> findByCompany_EmailOrderByCreatedAtDesc(String email);

    Optional<TeamRequest> findByTeamName(String teamName);

    long countByResponsibleAdminId(Integer adminId);

    long countByCompany_IdAndStateNot(Integer companyId, State state);

    long countByState(State state);

    @Query("""
            SELECT tr.responsibleAdminId AS adminId, COUNT(tr) AS total
            FROM TeamRequest tr
            WHERE tr.responsibleAdminId IS NOT NULL
              AND tr.state = :state
            GROUP BY tr.responsibleAdminId
            """)
    List<AdminAssignmentCount> countAssignmentsByState(@Param("state") State state);

    interface AdminAssignmentCount {
        Integer getAdminId();
        long getTotal();
    }
}
