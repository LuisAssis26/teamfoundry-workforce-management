package com.teamfoundry.backend.account.repository.preferences;

import com.teamfoundry.backend.account.model.preferences.PrefGeoArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrefGeoAreaRepository extends JpaRepository<PrefGeoArea, Integer> {
    Optional<PrefGeoArea> findByName(String name);

    Optional<PrefGeoArea> findByNameIgnoreCase(String name);
}
