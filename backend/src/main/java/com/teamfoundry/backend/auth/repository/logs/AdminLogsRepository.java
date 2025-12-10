package com.teamfoundry.backend.auth.repository.logs;

import com.teamfoundry.backend.auth.model.logs.AdminLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLogsRepository extends JpaRepository<AdminLogs, Integer> {
}
