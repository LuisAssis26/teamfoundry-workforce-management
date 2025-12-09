package com.teamfoundry.backend.account.service.company;

import com.teamfoundry.backend.account.dto.company.preferences.CompanyPreferencesListResponse;
import com.teamfoundry.backend.account.repository.preferences.PrefActivitySectorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyPreferencesService {

    private final PrefActivitySectorsRepository prefActivitySectorsRepository;

    public CompanyPreferencesListResponse loadOptions() {
        List<String> sectors = prefActivitySectorsRepository.findAll().stream()
                .map(com.teamfoundry.backend.account.model.preferences.PrefActivitySectors::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        List<String> countries = defaultCountries();
        return new CompanyPreferencesListResponse(sectors, countries);
    }

    private List<String> defaultCountries() {
        return List.of(
                "Portugal",
                "Espanha",
                "França",
                "Alemanha",
                "Reino Unido",
                "Itália",
                "Polónia"
        );
    }
}
