package com.teamfoundry.backend.account.dto.employee.profile;

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
    String curriculumFileName;
    String identificationFrontUrl;
    String identificationFrontFileName;
    String identificationBackUrl;
    String identificationBackFileName;
    String profilePictureUrl;
}
