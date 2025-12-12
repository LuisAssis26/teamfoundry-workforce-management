package com.teamfoundry.backend.superadmin.service.logs;

import com.teamfoundry.backend.auth.model.logs.AdminLogs;
import com.teamfoundry.backend.auth.model.logs.CommonLogs;
import com.teamfoundry.backend.auth.repository.logs.AdminLogsRepository;
import com.teamfoundry.backend.auth.repository.logs.CommonLogsRepository;
import com.teamfoundry.backend.superadmin.dto.logs.LogEntryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogsService {

    private final AdminLogsRepository adminLogsRepository;
    private final CommonLogsRepository commonLogsRepository;

    public List<LogEntryResponse> search(String type, Integer year, Integer month, String query, Integer limit) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (year != null && month != null) {
            LocalDate from = LocalDate.of(year, month, 1);
            start = from.atStartOfDay();
            end = from.plusMonths(1).atStartOfDay();
        } else if (year != null) {
            LocalDate from = LocalDate.of(year, 1, 1);
            start = from.atStartOfDay();
            end = from.plusYears(1).atStartOfDay();
        }

        String q = (query == null || query.isBlank()) ? null : query.trim();
        int max = limit != null && limit > 0 ? Math.min(limit, 500) : 200;

        boolean includeAdmins = !"USER".equalsIgnoreCase(type);
        boolean includeUsers = !"ADMIN".equalsIgnoreCase(type);

        List<LogEntryResponse> results = new ArrayList<>();

        if (includeAdmins) {
            List<AdminLogs> admins = adminLogsRepository.searchByPeriodAndQuery(start, end, q);
            admins.stream()
                    .map(a -> new LogEntryResponse("ADMIN", a.getAdmin().getUsername(), a.getAction(), a.getTimestamp()))
                    .forEach(results::add);
        }

        if (includeUsers) {
            List<CommonLogs> users = commonLogsRepository.searchByPeriodAndQuery(start, end, q);
            users.stream()
                    .map(u -> new LogEntryResponse("USER", u.getUser().getEmail(), u.getAction(), u.getTimestamp()))
                    .forEach(results::add);
        }

        results.sort(Comparator.comparing(LogEntryResponse::timestamp).reversed());
        if (results.size() > max) {
            return results.subList(0, max);
        }
        return results;
    }
}
