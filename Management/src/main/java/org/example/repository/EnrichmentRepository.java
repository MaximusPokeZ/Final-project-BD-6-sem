package org.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.model.Enrichment;

import java.util.List;

public interface EnrichmentRepository extends JpaRepository<Enrichment, Long> {
    List<Enrichment> getEnrichmentByEnrichmentId(long enrichmentId);
    Enrichment getEnrichmentByEnrichmentIdAndRuleId(long enrichmentId, long ruleId);
    void deleteEnrichmentByEnrichmentIdAndRuleId(long enrichmentId, long ruleId);
}
