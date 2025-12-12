package com.teamfoundry.backend.account.repository.preferences;

import com.teamfoundry.backend.account.model.preferences.PrefSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrefSkillRepository extends JpaRepository<PrefSkill, Integer> {
    Optional<PrefSkill> findByName(String name);

    Optional<PrefSkill> findByNameIgnoreCase(String name);
}
