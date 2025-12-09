package com.teamfoundry.backend.account_options.dto.employee;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class EmployeeProfileResponse {
    String firstName;
    String lastName;
    String gender;
    LocalDate birthDate;
    String nationality;
    Integer nif;
    String phone;
    String email;
    Boolean deactivated;
    String curriculumUrl;
    String identificationFrontUrl;
    String identificationBackUrl;
    String profilePictureUrl;
}
