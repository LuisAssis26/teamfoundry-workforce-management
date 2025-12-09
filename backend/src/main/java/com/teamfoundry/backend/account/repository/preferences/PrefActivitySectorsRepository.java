package com.teamfoundry.backend.account.repository.preferences;

import com.teamfoundry.backend.account.model.preferences.PrefActivitySectors;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrefActivitySectorsRepository extends JpaRepository<PrefActivitySectors, Integer> {
    List<PrefActivitySectors> findByNameIn(List<String> names);

    Optional<PrefActivitySectors> findByNameIgnoreCase(String name);
}
