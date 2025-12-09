package com.teamfoundry.backend.superadmin.repository.other;

import com.teamfoundry.backend.superadmin.model.other.WeeklyTip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyTipRepository extends JpaRepository<WeeklyTip, Long> {

    List<WeeklyTip> findAllByOrderByDisplayOrderAsc();

}

