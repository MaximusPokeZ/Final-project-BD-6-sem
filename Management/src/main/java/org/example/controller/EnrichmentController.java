package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.model.Enrichment;
import org.example.service.EnrichmentService;


@RestController
@RequestMapping("enrichment")
@AllArgsConstructor
public class EnrichmentController {

    private final EnrichmentService service;

    @GetMapping("/findAll")
    @Operation(summary = "Получить информацию о всех правилах обогащения в БД")
    public Iterable<Enrichment> getAllEnrichments() {
        return service.getAll();
    }

    @GetMapping("/findAll/{id}")
    @Operation(summary = "Получить информацию о всех правилах обогащения в БД по enrichment id")
    public Iterable<Enrichment> getAllEnrichmentsByEnrichmentId(@PathVariable long id) {
        return service.getEnrichmentsByEnrichmentId(id);
    }

    @GetMapping("/find/{enrichmentId}/{ruleId}")
    @Operation(summary = "Получить информацию о правиле обогащения по enrichment id и rule id")
    public Enrichment getEnrichmentById(@PathVariable long enrichmentId, @PathVariable long ruleId) {
        return service.getEnrichmentByEnrichmentIdAndRuleId(enrichmentId, ruleId);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить информацию о всех правилах обогащения")
    public void deleteEnrichment() {
        service.deleteAll();

    }

    @DeleteMapping("/delete/{enrichmentId}/{ruleId}")
    @Operation(summary = "Удалить информацию по конкретному правилу обогащения с enrichment id и rule id")
    public void deleteEnrichmentById(@PathVariable long enrichmentId, @PathVariable long ruleId) {
        service.deleteEnrichmentByEnrichmentIdAndRuleId(enrichmentId, ruleId);

    }

    @PostMapping("/save")
    @Operation(summary = "Создать правило обогащения")
    public ResponseEntity<Enrichment> save(@RequestBody @Valid Enrichment enrichment) {
        Enrichment savedEnrichment = service.saveEnrichment(enrichment);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedEnrichment);
    }

}
