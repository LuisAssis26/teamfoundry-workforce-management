package com.teamfoundry.backend.superadmin.repository.home;

import com.teamfoundry.backend.superadmin.model.home.IndustryShowcase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndustryShowcaseRepository extends JpaRepository<IndustryShowcase, Long> {

    List<IndustryShowcase> findAllByOrderByDisplayOrderAsc();
}
