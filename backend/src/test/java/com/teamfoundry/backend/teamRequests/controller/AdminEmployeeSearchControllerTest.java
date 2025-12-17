package com.teamfoundry.backend.teamRequests.controller;

import com.teamfoundry.backend.teamRequests.dto.search.AdminEmployeeSearchResponse;
import com.teamfoundry.backend.teamRequests.service.AdminEmployeeSearchService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminEmployeeSearchControllerTest {

    @Mock AdminEmployeeSearchService adminEmployeeSearchService;

    @InjectMocks AdminEmployeeSearchController controller;

    @Test
    void search_whenAreasAndSkillsNull_passesEmptyLists() {
        List<AdminEmployeeSearchResponse> expected = List.of(
                new AdminEmployeeSearchResponse(1, "Ana", "Silva", "ana@test.com", "999",
                        "dev", List.of(), List.of(), List.of())
        );
        when(adminEmployeeSearchService.search(eq("dev"), eq(Collections.emptyList()), eq(Collections.emptyList())))
                .thenReturn(expected);

        var result = controller.search("dev", null, null);

        assertThat(result).isEqualTo(expected);
        verify(adminEmployeeSearchService).search("dev", Collections.emptyList(), Collections.emptyList());
    }
}

