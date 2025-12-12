package com.teamfoundry.backend.account.dto.company.profile;

import jakarta.validation.constraints.NotBlank;

public class CompanyDeactivateAccountRequest {

    @NotBlank(message = "A password é obrigatória.")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
