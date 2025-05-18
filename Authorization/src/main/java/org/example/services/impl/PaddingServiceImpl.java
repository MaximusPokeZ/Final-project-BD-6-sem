package org.example.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.example.clients.EnrichmentClient;
import org.example.dto.request.PaddingRequest;
import org.example.dto.response.PaddingResponse;
import org.example.services.PaddingService;

@Service
@RequiredArgsConstructor
public class PaddingServiceImpl implements PaddingService {

  private final EnrichmentClient enrichmentClient;

  @Override
  public Iterable<PaddingResponse> getAllEnrichmentRequests() {
    return enrichmentClient.getAllEnrichments();
  }

  @Override
  public Iterable<PaddingResponse> getAllEnrichmentRequestsByEnrichmentRequestId(long id) {
    return enrichmentClient.getAllEnrichmentsByEnrichmentId(id);
  }

  @Override
  public PaddingResponse getEnrichmentRequestById(long enrichmentId, long ruleId) {
    return enrichmentClient.getEnrichmentById(enrichmentId, ruleId);
  }

  @Override
  public void deleteEnrichmentRequest() {
    enrichmentClient.deleteEnrichment();
  }

  @Override
  public void deleteEnrichmentRequestById(long enrichmentId, long ruleId) {
    enrichmentClient.deleteEnrichmentById(enrichmentId, ruleId);
  }

  @Override
  public void save(PaddingRequest enrichment) {
    enrichmentClient.save(enrichment);
  }
}
