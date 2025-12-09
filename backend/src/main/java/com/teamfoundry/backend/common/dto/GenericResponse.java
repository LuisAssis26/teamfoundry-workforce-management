package com.teamfoundry.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenericResponse {
    private final String message;
    private final String error;

    public static GenericResponse success(String message) {
        return new GenericResponse(message, null);
    }

    public static GenericResponse failure(String error) {
        return new GenericResponse(null, error);
    }
}
