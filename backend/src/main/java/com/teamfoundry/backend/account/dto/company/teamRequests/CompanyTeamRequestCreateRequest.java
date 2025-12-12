package com.teamfoundry.backend.account.dto.company.teamRequests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pedido para criar uma nova requisição de equipa pela empresa autenticada.
 */
@Data
public class CompanyTeamRequestCreateRequest {

    @NotBlank
    @Size(min = 3, max = 150)
    private String teamName;

    @Size(max = 500)
    private String description;

    @Size(max = 150)
    private String location;

    @FutureOrPresent(message = "A data de início não pode estar no passado.")
    private LocalDateTime startDate;

    @FutureOrPresent(message = "A data de fim não pode estar no passado.")
    private LocalDateTime endDate;

    @NotEmpty
    @Valid
    private List<RoleRequest> roles;

    @Data
    public static class RoleRequest {
        @NotBlank
        @Size(min = 2, max = 120)
        private String role;

        @Min(1)
        @Max(1000)
        private int quantity = 1;

        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal salary;
    }
}
