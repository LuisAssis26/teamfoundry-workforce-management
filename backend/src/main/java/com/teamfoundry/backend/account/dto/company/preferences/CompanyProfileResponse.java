package com.teamfoundry.backend.account.dto.company.preferences;

import lombok.Builder;
import lombok.Data;

/**
 * Resposta do perfil da empresa exibindo dados básicos da conta e do responsável.
 */
@Data
@Builder
public class CompanyProfileResponse {
    private Integer id;
    private String name;
    private Integer nif;
    private String email;
    private String address;
    private String country;
    private String phone;
    private String website;
    private String description;
    private boolean status;
    private Manager manager;

    @Data
    @Builder
    public static class Manager {
        private String name;
        private String email;
        private String phone;
        private String position;
    }
}
