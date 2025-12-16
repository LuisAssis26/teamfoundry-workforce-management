package com.teamfoundry.backend.teamRequests.repository;

import com.teamfoundry.backend.teamRequests.model.EmployeeRequestOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


/**
 * Ligações entre convites e vagas (EmployeeRequest).
 * Inclui contagem por função, desativação de convites e consultas por colaborador.
 */
public interface EmployeeRequestOfferRepository extends JpaRepository<EmployeeRequestOffer, Integer> {

    @Query("""
            SELECT ere.employeeRequest.requestedRole AS role, COUNT(ere) AS total
            FROM EmployeeRequestOffer ere
            WHERE ere.employeeRequest.teamRequest.id = :teamRequestId
              AND ere.active = true
            GROUP BY ere.employeeRequest.requestedRole
            """)
    List<RoleInviteCount> countInvitesByTeamRequest(@Param("teamRequestId") Integer teamRequestId);

    boolean existsByEmployeeRequest_IdAndEmployee_IdAndActiveTrue(int requestId, int employeeId);

    @Modifying
    @Query("""
            UPDATE EmployeeRequestOffer ere
            SET ere.active = false
            WHERE ere.employeeRequest.id = :requestId
              AND ere.active = true
              AND (:acceptedId IS NULL OR ere.employee.id <> :acceptedId)
            """)
    int deactivateInvitesForRequest(@Param("requestId") int requestId,
                                    @Param("acceptedId") Integer acceptedId);

    @Query("""
            SELECT DISTINCT ere.employee.id
            FROM EmployeeRequestOffer ere
            WHERE ere.employeeRequest.teamRequest.id = :teamId
              AND LOWER(ere.employeeRequest.requestedRole) = LOWER(:role)
              AND ere.active = true
            """)
    List<Integer> findActiveInviteEmployeeIdsByTeamAndRole(@Param("teamId") Integer teamId,
                                                           @Param("role") String role);

    @Query("""
            SELECT DISTINCT ere.employee.id
            FROM EmployeeRequestOffer ere
            WHERE ere.employeeRequest.teamRequest.id = :teamId
              AND ere.active = true
            """)
    List<Integer> findActiveInviteEmployeeIdsByTeam(@Param("teamId") Integer teamId);

    @Query("""
            SELECT ere
            FROM EmployeeRequestOffer ere
            JOIN FETCH ere.employeeRequest er
            JOIN FETCH er.teamRequest tr
            LEFT JOIN FETCH tr.company c
            WHERE LOWER(ere.employee.email) = LOWER(:email)
            """)
    List<EmployeeRequestOffer> findAllInvitesByEmployeeEmail(@Param("email") String email);

    @Query("""
            SELECT ere
            FROM EmployeeRequestOffer ere
            JOIN FETCH ere.employeeRequest er
            JOIN FETCH er.teamRequest tr
            JOIN FETCH tr.company c
            WHERE ere.active = true
              AND LOWER(ere.employee.email) = LOWER(:email)
            """)
    List<EmployeeRequestOffer> findActiveInvitesByEmployeeEmail(@Param("email") String email);


    interface RoleInviteCount {
        String getRole();
        long getTotal();
    }
}
