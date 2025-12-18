package com.teamfoundry.backend.account.service;

import com.teamfoundry.backend.account.dto.employee.profile.EmployeeProfileResponse;
import com.teamfoundry.backend.account.dto.employee.profile.EmployeeProfileUpdateRequest;
import com.teamfoundry.backend.account.model.employee.profile.EmployeeAccount;
import com.teamfoundry.backend.account.repository.employee.EmployeeAccountRepository;
import com.teamfoundry.backend.account.service.employee.EmployeeProfileAndDocumentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeProfileAndDocumentsServiceTest {

    @Mock
    private EmployeeAccountRepository employeeAccountRepository;

    @InjectMocks
    private EmployeeProfileAndDocumentsService employeeProfileAndDocumentsService;

    private EmployeeAccount sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = new EmployeeAccount();
        sampleAccount.setEmail("employee@example.com");
        sampleAccount.setName("Joao");
        sampleAccount.setSurname("Silva");
        sampleAccount.setGender("MALE");
        sampleAccount.setBirthDate(LocalDate.of(1992, 1, 15));
        sampleAccount.setNationality("Portugal");
        sampleAccount.setNif(123456789);
        sampleAccount.setPhone("+351912345678");
    }

    @Test
    void getProfileReturnsMappedResponse() {
        when(employeeAccountRepository.findByEmail("employee@example.com"))
                .thenReturn(Optional.of(sampleAccount));

        EmployeeProfileResponse response = employeeProfileAndDocumentsService.getProfile("Employee@Example.com  ");

        assertThat(response.getFirstName()).isEqualTo("Joao");
        assertThat(response.getLastName()).isEqualTo("Silva");
        assertThat(response.getGender()).isEqualTo("MALE");
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1992, 1, 15));
        assertThat(response.getNationality()).isEqualTo("Portugal");
        assertThat(response.getNif()).isEqualTo(123456789);
        assertThat(response.getPhone()).isEqualTo("+351912345678");
        assertThat(response.getEmail()).isEqualTo("employee@example.com");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        verify(employeeAccountRepository).findByEmail(emailCaptor.capture());
        assertThat(emailCaptor.getValue()).isEqualTo("employee@example.com");
    }

    @Test
    void updateProfileUpdatesAndPersistsEntity() {
        when(employeeAccountRepository.findByEmail("employee@example.com"))
                .thenReturn(Optional.of(sampleAccount));
        when(employeeAccountRepository.save(any(EmployeeAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeProfileUpdateRequest request = new EmployeeProfileUpdateRequest();
        request.setFirstName("Maria");
        request.setLastName("Sousa");
        request.setGender("female");
        request.setBirthDate(LocalDate.of(1995, 5, 10));
        request.setNationality("Portugal");
        request.setNif(987654321);
        request.setPhone("+351999888777");

        EmployeeProfileResponse response = employeeProfileAndDocumentsService.updateProfile("employee@example.com", request);

        assertThat(response.getFirstName()).isEqualTo("Maria");
        assertThat(response.getLastName()).isEqualTo("Sousa");
        assertThat(response.getGender()).isEqualTo("FEMALE");
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 10));
        assertThat(response.getNationality()).isEqualTo("Portugal");
        assertThat(response.getNif()).isEqualTo(987654321);
        assertThat(response.getPhone()).isEqualTo("+351999888777");

        assertThat(sampleAccount.getName()).isEqualTo("Maria");
        assertThat(sampleAccount.getSurname()).isEqualTo("Sousa");
        assertThat(sampleAccount.getGender()).isEqualTo("FEMALE");
        assertThat(sampleAccount.getBirthDate()).isEqualTo(LocalDate.of(1995, 5, 10));
        assertThat(sampleAccount.getNationality()).isEqualTo("Portugal");
        assertThat(sampleAccount.getNif()).isEqualTo(987654321);
        assertThat(sampleAccount.getPhone()).isEqualTo("+351999888777");
    }

    @Test
    void getProfileWithoutEmailThrowsUnauthorized() {
        assertThatThrownBy(() -> employeeProfileAndDocumentsService.getProfile("   "))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.UNAUTHORIZED);
    }
}

