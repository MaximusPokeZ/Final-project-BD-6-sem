package org.example.services;

import org.example.dto.request.PaddingRequest;
import org.example.dto.response.PaddingResponse;

public interface PaddingService {

  Iterable<PaddingResponse> getAllEnrichmentRequests();

  Iterable<PaddingResponse> getAllEnrichmentRequestsByEnrichmentRequestId(long id);

  PaddingResponse getEnrichmentRequestById(long enrichmentId, long ruleId);

  void deleteEnrichmentRequest();

  void deleteEnrichmentRequestById(long enrichmentId, long ruleId);

  void save(PaddingRequest enrichment);
}
