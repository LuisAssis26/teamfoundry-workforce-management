package com.teamfoundry.backend.teamRequests.dto.search;

import java.time.LocalDate;
import java.util.List;

/**
 * Resposta com o perfil completo do colaborador para visualização por admin,
 * incluindo experiências concluídas (máx. 2).
 */
public record AdminEmployeeProfileResponse(
        Integer id,
        String firstName,
        String lastName,
        String gender,
        LocalDate birthDate,
        String nationality,
        String phone,
        String email,
        String preferredRole,
        List<String> skills,
        List<String> areas,
        List<String> experiences // Ex.: "Equipe X - Soldador (2024-01-10)"
) {}
