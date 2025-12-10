package com.teamfoundry.backend.auth.repository.logs;

import com.teamfoundry.backend.auth.model.logs.CommonLogs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonLogsRepository extends JpaRepository<CommonLogs, Integer> {
}
