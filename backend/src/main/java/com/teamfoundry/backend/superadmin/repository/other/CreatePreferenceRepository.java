package com.teamfoundry.backend.superadmin.repository.other;

import com.teamfoundry.backend.account.model.preferences.PrefRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório dedicado à criação e pesquisa de funções (tabela funcao).
 */
public interface CreatePreferenceRepository extends JpaRepository<PrefRole, Integer> {

    /**
     * Procura uma função existente ignorando maiúsculas/minúsculas.
     */
    Optional<PrefRole> findByNameIgnoreCase(String name);
}
