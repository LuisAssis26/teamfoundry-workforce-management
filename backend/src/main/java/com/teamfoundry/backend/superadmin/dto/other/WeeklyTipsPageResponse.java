package com.teamfoundry.backend.superadmin.dto.other;

import java.util.List;

public record WeeklyTipsPageResponse(
        WeeklyTipResponse tipOfWeek,
        List<WeeklyTipResponse> tips
) {
}

