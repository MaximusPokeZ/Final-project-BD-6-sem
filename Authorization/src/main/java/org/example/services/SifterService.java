package org.example.services;

import org.example.dto.request.SifterRequest;
import org.example.dto.response.SifterResponse;

public interface SifterService {

  Iterable<SifterResponse> getAllFilters();

  Iterable<SifterResponse> getAllFiltersByFilterId(long id);

  SifterResponse getFilterByFilterIdAndRuleId(long filterId, long ruleId);

  void deleteFilter();

  void deleteFilterById(long filterId, long ruleId);

  void save(SifterRequest filter);
}
