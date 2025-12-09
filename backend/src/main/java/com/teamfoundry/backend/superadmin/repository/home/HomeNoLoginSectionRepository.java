package com.teamfoundry.backend.superadmin.repository.home;

import com.teamfoundry.backend.superadmin.model.home.HomeNoLoginSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeNoLoginSectionRepository extends JpaRepository<HomeNoLoginSection, Long> {

    List<HomeNoLoginSection> findAllByOrderByDisplayOrderAsc();

}
