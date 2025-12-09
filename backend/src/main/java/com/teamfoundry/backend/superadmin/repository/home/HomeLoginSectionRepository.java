package com.teamfoundry.backend.superadmin.repository.home;

import com.teamfoundry.backend.superadmin.model.home.HomeLoginSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeLoginSectionRepository extends JpaRepository<HomeLoginSection, Long> {

    List<HomeLoginSection> findAllByOrderByDisplayOrderAsc();
}
