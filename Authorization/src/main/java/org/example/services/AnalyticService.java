package org.example.services;

import org.example.dto.request.AnalyticRequest;
import org.example.dto.response.AnalyticResponse;

public interface AnalyticService {
    Iterable<AnalyticResponse> getAllAnalyticRequests();

    Iterable<AnalyticResponse> getAllAnalyticRequestsByServiceId(long id);

    void deleteAnalyticRequest();

    void save(AnalyticRequest analyticRequest);
}
