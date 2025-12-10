package com.teamfoundry.backend.auth.repository.logs;

import com.teamfoundry.backend.auth.model.logs.AdminLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminLogsRepository extends JpaRepository<AdminLogs, Integer> {
    @Query("""
            SELECT al
            FROM AdminLogs al
            JOIN al.admin a
            WHERE al.timestamp >= COALESCE(:start, al.timestamp)
              AND al.timestamp <  COALESCE(:end, al.timestamp)
              AND LOWER(a.username) LIKE LOWER(
                    COALESCE(CONCAT('%', :query, '%'), a.username)
                  )
            ORDER BY al.timestamp DESC
            """)
    List<AdminLogs> searchByPeriodAndQuery(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("query") String query);
}
