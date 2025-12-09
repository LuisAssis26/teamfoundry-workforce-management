package com.teamfoundry.backend.accountOptions.controller;

import com.teamfoundry.backend.account.controller.employee.profile.EmployeeProfileController;
import com.teamfoundry.backend.account.dto.employee.profile.EmployeeProfileResponse;
import com.teamfoundry.backend.account.dto.employee.profile.EmployeeProfileUpdateRequest;
import com.teamfoundry.backend.account.service.employee.EmployeeProfileAndDocumentsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeProfileControllerTest {

    @Mock
    private EmployeeProfileAndDocumentsService employeeProfileAndDocumentsService;

    @InjectMocks
    private EmployeeProfileController controller;

    @Test
    @DisplayName("GET profile delega para o service com o email do utilizador autenticado")
    void getProfile_delegatesToServiceWithAuthenticatedEmail() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@test.com");

        EmployeeProfileResponse expected = EmployeeProfileResponse.builder()
                .firstName("Joao")
                .lastName("Silva")
                .gender("MALE")
                .birthDate(LocalDate.of(1992, 1, 15))
                .nationality("Portugal")
                .nif(123456789)
                .phone("+351912345678")
                .email("user@test.com")
                .build();

        when(employeeProfileAndDocumentsService.getProfile("user@test.com")).thenReturn(expected);

        EmployeeProfileResponse result = controller.getProfile(authentication);

        assertThat(result).isEqualTo(expected);
        verify(employeeProfileAndDocumentsService).getProfile("user@test.com");
    }

    @Test
    @DisplayName("PUT profile envia request e email para o service")
    void updateProfile_delegatesToServiceWithRequestAndEmail() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@test.com");

        EmployeeProfileUpdateRequest request = new EmployeeProfileUpdateRequest();
        request.setFirstName("Maria");
        request.setLastName("Sousa");

        EmployeeProfileResponse expected = EmployeeProfileResponse.builder()
                .firstName("Maria")
                .lastName("Sousa")
                .email("user@test.com")
                .build();

        when(employeeProfileAndDocumentsService.updateProfile(eq("user@test.com"), any(EmployeeProfileUpdateRequest.class)))
                .thenReturn(expected);

        EmployeeProfileResponse result = controller.updateProfile(request, authentication);

        assertThat(result).isEqualTo(expected);
        verify(employeeProfileAndDocumentsService).updateProfile("user@test.com", request);
    }

    @Test
    @DisplayName("GET profile sem autenticacao propaga excecao de Unauthorized do service")
    void getProfile_withoutAuthentication_throwsUnauthorized() {
        when(employeeProfileAndDocumentsService.getProfile(null))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> controller.getProfile(null))
                .isInstanceOf(ResponseStatusException.class);
    }
}
