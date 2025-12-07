package com.teamfoundry.backend.admin.repository;

import com.teamfoundry.backend.admin.model.EmployeeRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Vagas (EmployeeRequest): consultas para convites, contagens e hist┴ico.
 */
public interface EmployeeRequestRepository extends JpaRepository<EmployeeRequest, Integer> {

    @EntityGraph(attributePaths = {"teamRequest", "teamRequest.company"})
    List<EmployeeRequest> findByEmployee_EmailOrderByAcceptedDateDesc(String email);

    @Query("""
            SELECT er.teamRequest.id AS requestId, COUNT(er) AS total
            FROM EmployeeRequest er
            WHERE er.teamRequest.id IN :requestIds
            GROUP BY er.teamRequest.id
            """)
    List<TeamRequestCount> countByTeamRequestIds(@Param("requestIds") Collection<Integer> requestIds);

    @Query("""
            SELECT er.requestedRole AS role, COUNT(er) AS total, COUNT(er.employee) AS filled
            FROM EmployeeRequest er
            WHERE er.teamRequest.id = :teamRequestId
            GROUP BY er.requestedRole
            """)
    List<RoleCount> countByRoleForTeam(@Param("teamRequestId") Integer teamRequestId);

    @EntityGraph(attributePaths = {"teamRequest"})
    List<EmployeeRequest> findByTeamRequest_IdAndRequestedRoleIgnoreCaseAndEmployeeIsNull(Integer teamRequestId, String role);

    @Query("""
            SELECT DISTINCT er.employee.id
            FROM EmployeeRequest er
            WHERE er.teamRequest.id = :teamId
              AND er.employee IS NOT NULL
            """)
    List<Integer> findAcceptedEmployeeIdsByTeam(@Param("teamId") Integer teamId);

    @Query("""
            SELECT COUNT(er)
            FROM EmployeeRequest er
            WHERE er.teamRequest.id = :teamId
              AND er.employee.id = :employeeId
            """)
    long countAcceptedForTeam(@Param("teamId") Integer teamId, @Param("employeeId") Integer employeeId);

    @Query("""
            SELECT COUNT(er)
            FROM EmployeeRequest er
            JOIN er.teamRequest tr
            WHERE er.employee.id = :employeeId
              AND er.id <> :currentRequestId
              AND tr.startDate IS NOT NULL
              AND tr.endDate IS NOT NULL
              AND tr.startDate <= :endDate
              AND tr.endDate >= :startDate
            """)
    long countOverlappingAccepted(
            @Param("employeeId") Integer employeeId,
            @Param("currentRequestId") Integer currentRequestId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @EntityGraph(attributePaths = {"teamRequest", "teamRequest.company"})
    List<EmployeeRequest> findByEmployee_IdOrderByAcceptedDateDesc(Integer employeeId);

    interface TeamRequestCount {
        Integer getRequestId();
        long getTotal();
    }

    interface RoleCount {
        String getRole();
        long getTotal();
        long getFilled();
    }
}
