package org.example.services;

import org.example.dto.request.SelectionRequest;
import org.example.dto.response.SelectionResponse;

public interface SelectionService {

  Iterable<SelectionResponse> getAllDeduplications();

  Iterable<SelectionResponse> getAllDeduplicationsByDeduplicationId(long id);

  SelectionResponse getDeduplicationById(long deduplicationId, long ruleId);

  void deleteDeduplication();

  void deleteDeduplicationById(long deduplicationId, long ruleId);

  void save(SelectionRequest deduplication);
}