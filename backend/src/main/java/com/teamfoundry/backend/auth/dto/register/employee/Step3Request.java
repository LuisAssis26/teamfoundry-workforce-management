package com.teamfoundry.backend.auth.dto.register.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Step3Request {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String role;

    @NotEmpty
    private List<String> areas;

    @NotEmpty
    private List<String> skills;

    @NotNull
    private Boolean termsAccepted;
}
