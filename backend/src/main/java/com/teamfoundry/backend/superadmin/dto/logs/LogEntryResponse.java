package com.teamfoundry.backend.superadmin.dto.logs;

import java.time.LocalDateTime;

public record LogEntryResponse(
        String type,      // ADMIN ou USER
        String actor,     // username ou email
        String action,
        LocalDateTime timestamp
) {
}
