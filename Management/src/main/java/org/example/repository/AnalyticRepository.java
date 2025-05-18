package org.example.repository;

import org.example.model.Analytic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalyticRepository extends JpaRepository<Analytic, Long> {
    List<Analytic> getAnalyticByServiceId(long serviceId);
    void deleteByServiceId(long serviceId);
}
