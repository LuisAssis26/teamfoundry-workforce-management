package com.teamfoundry.backend.superadmin.service.logs;

import com.teamfoundry.backend.account.model.Account;
import com.teamfoundry.backend.auth.model.logs.AdminLogs;
import com.teamfoundry.backend.auth.model.logs.CommonLogs;
import com.teamfoundry.backend.auth.repository.logs.AdminLogsRepository;
import com.teamfoundry.backend.auth.repository.logs.CommonLogsRepository;
import com.teamfoundry.backend.superadmin.dto.logs.LogEntryResponse;
import com.teamfoundry.backend.superadmin.model.credentials.AdminAccount;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LogsServiceTest {

    @Mock AdminLogsRepository adminLogsRepository;
    @Mock CommonLogsRepository commonLogsRepository;

    @InjectMocks LogsService service;

    @Test
    void searchWithYearAndMonthFiltersBothRepositoriesAndSorts() {
        LocalDateTime adminTime = LocalDateTime.of(2024, 5, 20, 10, 0);
        LocalDateTime userTime = LocalDateTime.of(2024, 5, 1, 12, 0);

        when(adminLogsRepository.searchByPeriodAndQuery(any(), any(), any()))
                .thenReturn(List.of(adminLog("alpha", "updated", adminTime)));
        when(commonLogsRepository.searchByPeriodAndQuery(any(), any(), any()))
                .thenReturn(List.of(userLog("user@test.com", "login", userTime)));

        List<LogEntryResponse> results = service.search(null, 2024, 5, "  John  ", null);

        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        verify(adminLogsRepository).searchByPeriodAndQuery(startCaptor.capture(), endCaptor.capture(), queryCaptor.capture());
        verify(commonLogsRepository).searchByPeriodAndQuery(startCaptor.capture(), endCaptor.capture(), queryCaptor.capture());

        List<LocalDateTime> capturedStarts = startCaptor.getAllValues();
        List<LocalDateTime> capturedEnds = endCaptor.getAllValues();
        List<String> capturedQueries = queryCaptor.getAllValues();

        assertThat(capturedStarts).containsOnly(LocalDateTime.of(2024, 5, 1, 0, 0));
        assertThat(capturedEnds).containsOnly(LocalDateTime.of(2024, 6, 1, 0, 0));
        assertThat(capturedQueries).containsOnly("John");

        assertThat(results).hasSize(2);
        assertThat(results.get(0).timestamp()).isEqualTo(adminTime);
        assertThat(results.get(0).type()).isEqualTo("ADMIN");
        assertThat(results.get(1).timestamp()).isEqualTo(userTime);
        assertThat(results.get(1).type()).isEqualTo("USER");
    }

    @Test
    void searchWithUserTypeOnlyQueriesUsersAndAppliesLimit() {
        LocalDateTime newer = LocalDateTime.now();
        LocalDateTime older = newer.minusDays(1);
        when(commonLogsRepository.searchByPeriodAndQuery(null, null, "test"))
                .thenReturn(List.of(
                        userLog("second@test.com", "logout", older),
                        userLog("first@test.com", "login", newer)
                ));

        List<LogEntryResponse> results = service.search("USER", null, null, "test", 1);

        verify(adminLogsRepository, never()).searchByPeriodAndQuery(any(), any(), any());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).actor()).isEqualTo("first@test.com");
        assertThat(results.get(0).type()).isEqualTo("USER");
        assertThat(results.get(0).timestamp()).isEqualTo(newer);
    }

    @Test
    void searchWithAdminTypeCapsLimitAndSkipsUserRepository() {
        LocalDateTime base = LocalDateTime.of(2024, 1, 1, 0, 0);
        List<AdminLogs> logs = IntStream.range(0, 600)
                .mapToObj(i -> adminLog("admin" + i, "action" + i, base.plusMinutes(i)))
                .toList();
        when(adminLogsRepository.searchByPeriodAndQuery(null, null, null)).thenReturn(logs);

        List<LogEntryResponse> results = service.search("ADMIN", null, null, null, 800);

        verify(commonLogsRepository, never()).searchByPeriodAndQuery(any(), any(), any());
        assertThat(results).hasSize(500);
        assertThat(results.get(0).actor()).isEqualTo("admin599");
        assertThat(results.get(0).timestamp()).isEqualTo(base.plusMinutes(599));
    }

    private AdminLogs adminLog(String username, String action, LocalDateTime timestamp) {
        AdminAccount admin = new AdminAccount();
        admin.setUsername(username);

        AdminLogs log = new AdminLogs();
        log.setAdmin(admin);
        log.setAction(action);
        log.setTimestamp(timestamp);
        return log;
    }

    private CommonLogs userLog(String email, String action, LocalDateTime timestamp) {
        Account account = new Account();
        account.setEmail(email);

        CommonLogs log = new CommonLogs();
        log.setUser(account);
        log.setAction(action);
        log.setTimestamp(timestamp);
        return log;
    }
}

