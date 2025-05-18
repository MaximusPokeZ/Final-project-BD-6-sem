package org.example.clients;


import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.dto.request.PaddingRequest;
import org.example.dto.response.PaddingResponse;


@FeignClient(
    name = "enrichmentClient",
    url = "${feign.client.url.enrichment}"
)
public interface EnrichmentClient {
    @GetMapping("/findAll")
    Iterable<PaddingResponse> getAllEnrichments();

    @GetMapping("/findAll/{id}")
    Iterable<PaddingResponse> getAllEnrichmentsByEnrichmentId(@PathVariable long id);

    @GetMapping("/find/{enrichmentId}/{ruleId}")
    PaddingResponse getEnrichmentById(@PathVariable long enrichmentId, @PathVariable long ruleId);

    @DeleteMapping("/delete")
    void deleteEnrichment();

    @DeleteMapping("/delete/{enrichmentId}/{ruleId}")
    void deleteEnrichmentById(@PathVariable long enrichmentId, @PathVariable long ruleId);

    @PostMapping("/save")
    ResponseEntity<PaddingResponse> save(@RequestBody @Valid PaddingRequest enrichment);
}
