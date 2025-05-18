package org.example.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.example.clients.FilterClient;
import org.example.dto.request.SifterRequest;
import org.example.dto.response.SifterResponse;
import org.example.services.SifterService;

@Service
@RequiredArgsConstructor
public class SifterServiceImpl implements SifterService {

  private final FilterClient filterClient;

  @Override
  public Iterable<SifterResponse> getAllFilters() {
    return filterClient.getAllFilters();
  }

  @Override
  public Iterable<SifterResponse> getAllFiltersByFilterId(long id) {
    return filterClient.getAllFiltersByFilterId(id);
  }

  @Override
  public SifterResponse getFilterByFilterIdAndRuleId(long filterId, long ruleId) {
    return filterClient.getFilterByFilterIdAndRuleId(filterId, ruleId);
  }

  @Override
  public void deleteFilter() {
    filterClient.deleteFilter();
  }

  @Override
  public void deleteFilterById(long filterId, long ruleId) {
    filterClient.deleteFilterById(filterId, ruleId);
  }

  @Override
  public void save(SifterRequest filter) {
    filterClient.save(filter);
  }
}
