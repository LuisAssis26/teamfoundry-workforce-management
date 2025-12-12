package com.teamfoundry.backend.account.dto.employee.preferences;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class EmployeePreferencesResponse {

    String role;
    List<String> skills;
    List<String> areas;
}
