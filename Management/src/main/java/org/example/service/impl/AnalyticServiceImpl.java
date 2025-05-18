package org.example.service.impl;

import lombok.AllArgsConstructor;
import org.example.model.Analytic;
import org.example.model.Enrichment;
import org.example.repository.AnalyticRepository;
import org.example.service.AnalyticService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AnalyticServiceImpl implements AnalyticService {
    private final AnalyticRepository analyticRepository;

    @Override
    public List<Analytic> getAll() {
        return analyticRepository.findAll();
    }

    @Override
    public List<Analytic> getAnalyticsByServiceId(long serviceId) {
        return analyticRepository.getAnalyticByServiceId(serviceId);
    }

    @Override
    public void deleteAll() {
        analyticRepository.deleteAll();
    }

    @Override
    public void deleteAnalyticByServiceId(long serviceId) {
        analyticRepository.deleteByServiceId(serviceId);
    }

    @Override
    public Analytic saveAnalytic(Analytic analytic) {
        return analyticRepository.save(analytic);
    }
}
