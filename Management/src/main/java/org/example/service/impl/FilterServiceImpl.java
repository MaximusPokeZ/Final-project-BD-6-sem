package org.example.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.model.Filter;
import org.example.repository.FilterRepository;
import org.example.service.FilterService;

import java.util.List;

@Service
@AllArgsConstructor
public class FilterServiceImpl implements FilterService {
    private final FilterRepository filterRepository;

    @Override
    public List<Filter> getAllFilters() {
        return filterRepository.findAll();
    }

    @Override
    public List<Filter> getAllFiltersByFilterId(long filterId) {
        return filterRepository.getAllByFilterId(filterId);
    }

    @Override
    public Filter getFilterByFilterIdAndRuleId(long filterId, long ruleId) {
        return filterRepository.getFilterByFilterIdAndRuleId(filterId, ruleId);
    }

    @Override
    public void deleteAllFilters() {
        filterRepository.deleteAll();
    }

    @Override
    @Transactional
    public void deleteFilterByFilterIdAndRuleID(long filterId, long ruleId) {
        filterRepository.deleteFilterByFilterIdAndRuleId(filterId, ruleId);
    }

    @Override
    public Filter saveFilter(Filter filter) {
        return filterRepository.save(filter);
    }


}
