package com.teamfoundry.backend.account.repository.preferences;

import com.teamfoundry.backend.account.model.preferences.PrefRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrefRoleRepository extends JpaRepository<PrefRole, Integer> {
    Optional<PrefRole> findByName(String name);
}
