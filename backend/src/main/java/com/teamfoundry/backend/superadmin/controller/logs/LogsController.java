package com.teamfoundry.backend.superadmin.controller.logs;

import com.teamfoundry.backend.superadmin.dto.logs.LogEntryResponse;
import com.teamfoundry.backend.superadmin.service.logs.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/logs")
@RequiredArgsConstructor
public class LogsController {

    private final LogsService logsService;

    @GetMapping
    public List<LogEntryResponse> searchLogs(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return logsService.search(type, year, month, query, limit);
    }
}
