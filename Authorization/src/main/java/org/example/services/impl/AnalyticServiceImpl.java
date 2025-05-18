package org.example.services.impl;

import lombok.AllArgsConstructor;
import org.example.clients.AnalyticClient;
import org.example.dto.request.AnalyticRequest;
import org.example.dto.response.AnalyticResponse;
import org.example.services.AnalyticService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnalyticServiceImpl implements AnalyticService {

    private final AnalyticClient analyticClient;

    @Override
    public Iterable<AnalyticResponse> getAllAnalyticRequests() {
        return analyticClient.getAllAnalytics();
    }

    @Override
    public Iterable<AnalyticResponse> getAllAnalyticRequestsByServiceId(long id) {
        return analyticClient.getAllAnalyticsByServiceId(id);
    }

    @Override
    public void deleteAnalyticRequest() {
        analyticClient.deleteAllAnalytics();
    }

    @Override
    public void save(AnalyticRequest analyticRequest) {
        analyticClient.save(analyticRequest);
    }
}
