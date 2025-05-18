package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.model.Deduplication;

import java.util.List;

public interface DedublicationRepository extends JpaRepository<Deduplication, Long> {
    List<Deduplication> findDeduplicationByDeduplicationId(long dedublicationId);
    Deduplication findDeduplicationByDeduplicationIdAndRuleId(long deduplicationId, long ruleId);
    void deleteDeduplicationByDeduplicationIdAndRuleId(long deduplicationId, long ruleId);
}
