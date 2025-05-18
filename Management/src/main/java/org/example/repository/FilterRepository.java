package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.model.Filter;

import java.util.List;

public interface FilterRepository extends JpaRepository<Filter, Long> {
    List<Filter> getAllByFilterId(long filterId);
    Filter getFilterByFilterIdAndRuleId(long filterId, long ruleId);
    void deleteFilterByFilterIdAndRuleId(long filterId, long ruleId);
}
