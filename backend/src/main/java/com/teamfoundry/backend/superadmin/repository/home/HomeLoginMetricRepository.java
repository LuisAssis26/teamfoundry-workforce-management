package com.teamfoundry.backend.superadmin.repository.home;

import com.teamfoundry.backend.superadmin.model.home.HomeLoginMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeLoginMetricRepository extends JpaRepository<HomeLoginMetric, Long> {
    List<HomeLoginMetric> findAllByOrderByDisplayOrderAsc();
}
