package com.teamfoundry.backend.superadmin.repository.home;

import com.teamfoundry.backend.superadmin.model.home.PartnerShowcase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartnerShowcaseRepository extends JpaRepository<PartnerShowcase, Long> {

    List<PartnerShowcase> findAllByOrderByDisplayOrderAsc();
}
