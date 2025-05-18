package org.example.service;

import org.example.model.Enrichment;

import java.util.List;

public interface EnrichmentService {
    List<Enrichment> getAll();
    List<Enrichment> getEnrichmentsByEnrichmentId(long enrichmentId);
    Enrichment getEnrichmentByEnrichmentIdAndRuleId(long enrichmentId, long ruleId);
    void deleteAll();
    void deleteEnrichmentByEnrichmentIdAndRuleId(long enrichmentId, long ruleId);
    Enrichment saveEnrichment(Enrichment enrichment);
}
