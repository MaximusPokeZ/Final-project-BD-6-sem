package org.example.service;

import org.example.model.Analytic;
import org.example.model.Enrichment;
import org.example.repository.AnalyticRepository;

import java.util.List;

public interface AnalyticService {
    List<Analytic> getAll();
    List<Analytic> getAnalyticsByServiceId(long serviceId);
    void deleteAll();
    void deleteAnalyticByServiceId(long serviceId);
    Analytic saveAnalytic(Analytic analytic);
}
