package com.teamfoundry.backend.auth.repository.logs;

import com.teamfoundry.backend.auth.model.logs.CommonLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CommonLogsRepository extends JpaRepository<CommonLogs, Integer> {

    @Query("""
            SELECT c
            FROM CommonLogs c
            JOIN c.user u
            WHERE c.timestamp >= COALESCE(:start, c.timestamp)
              AND c.timestamp <  COALESCE(:end, c.timestamp)
              AND LOWER(u.email) LIKE LOWER(
                    COALESCE(CONCAT('%', :query, '%'), u.email)
                  )
            ORDER BY c.timestamp DESC
            """)
    List<CommonLogs> searchByPeriodAndQuery(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end,
                                            @Param("query") String query);
}
