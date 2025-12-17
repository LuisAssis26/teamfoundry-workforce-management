package com.teamfoundry.backend.teamRequests.dto.search;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AdminEmployeeProfileResponseTest {

    @Test
    void record_structure_matches_contract() {
        assertThat(AdminEmployeeProfileResponse.class.isRecord()).isTrue();

        RecordComponent[] components = AdminEmployeeProfileResponse.class.getRecordComponents();
        assertThat(components).hasSize(12);

        assertThat(components).extracting(RecordComponent::getName).containsExactly(
                "id",
                "firstName",
                "lastName",
                "gender",
                "birthDate",
                "nationality",
                "phone",
                "email",
                "preferredRole",
                "skills",
                "areas",
                "experiences"
        );

        assertThat(components).extracting(RecordComponent::getType).containsExactly(
                Integer.class,
                String.class,
                String.class,
                String.class,
                LocalDate.class,
                String.class,
                String.class,
                String.class,
                String.class,
                List.class,
                List.class,
                List.class
        );
    }

    @Test
    void accessors_return_constructor_values() {
        LocalDate birthDate = LocalDate.of(1999, 5, 12);
        List<String> skills = List.of("Java", "Spring");
        List<String> areas = List.of("Lisboa");
        List<String> experiences = List.of("Equipe X - Developer (2024-01-10)");

        AdminEmployeeProfileResponse response = new AdminEmployeeProfileResponse(
                10,
                "Ana",
                "Silva",
                "F",
                birthDate,
                "PT",
                "999",
                "ana@example.com",
                "Developer",
                skills,
                areas,
                experiences
        );

        assertThat(response.id()).isEqualTo(10);
        assertThat(response.firstName()).isEqualTo("Ana");
        assertThat(response.lastName()).isEqualTo("Silva");
        assertThat(response.gender()).isEqualTo("F");
        assertThat(response.birthDate()).isEqualTo(birthDate);
        assertThat(response.nationality()).isEqualTo("PT");
        assertThat(response.phone()).isEqualTo("999");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.preferredRole()).isEqualTo("Developer");
        assertThat(response.skills()).isSameAs(skills);
        assertThat(response.areas()).isSameAs(areas);
        assertThat(response.experiences()).isSameAs(experiences);
    }

    @Test
    void equals_and_hashcode_are_value_based() {
        LocalDate birthDate = LocalDate.of(2000, 1, 1);
        List<String> skills = List.of("Java");
        List<String> areas = List.of("Porto");
        List<String> experiences = List.of("Equipe A - QA (2023-10-01)");

        AdminEmployeeProfileResponse a = new AdminEmployeeProfileResponse(
                1,
                "Joao",
                "Pereira",
                "M",
                birthDate,
                "PT",
                "123",
                "joao@example.com",
                "QA",
                skills,
                areas,
                experiences
        );

        AdminEmployeeProfileResponse b = new AdminEmployeeProfileResponse(
                1,
                "Joao",
                "Pereira",
                "M",
                birthDate,
                "PT",
                "123",
                "joao@example.com",
                "QA",
                skills,
                areas,
                experiences
        );

        AdminEmployeeProfileResponse different = new AdminEmployeeProfileResponse(
                2,
                "Joao",
                "Pereira",
                "M",
                birthDate,
                "PT",
                "123",
                "joao@example.com",
                "QA",
                skills,
                areas,
                experiences
        );

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(different);
    }
}

