package org.example.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.example.clients.DeduplicationClient;
import org.example.dto.request.SelectionRequest;
import org.example.dto.response.SelectionResponse;
import org.example.services.SelectionService;

@Service
@RequiredArgsConstructor
public class SelectionServiceImpl implements SelectionService {

  private final DeduplicationClient deduplicationClient;

  @Override
  public Iterable<SelectionResponse> getAllDeduplications() {
    return deduplicationClient.getAllDeduplications();
  }

  @Override
  public Iterable<SelectionResponse> getAllDeduplicationsByDeduplicationId(long id) {
    return deduplicationClient.getAllDeduplicationsByDeduplicationId(id);
  }

  @Override
  public SelectionResponse getDeduplicationById(long deduplicationId, long ruleId) {
    return deduplicationClient.getDeduplicationById(deduplicationId, ruleId);
  }

  @Override
  public void deleteDeduplication() {
    deduplicationClient.deleteDeduplication();
  }

  @Override
  public void deleteDeduplicationById(long deduplicationId, long ruleId) {
    deduplicationClient.deleteDeduplicationById(deduplicationId, ruleId);
  }

  @Override
  public void save(SelectionRequest deduplication) {
    deduplicationClient.save(deduplication);
  }
}
