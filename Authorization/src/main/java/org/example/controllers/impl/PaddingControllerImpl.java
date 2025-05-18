package org.example.controllers.impl;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.example.controllers.PaddingController;
import org.example.dto.request.PaddingRequest;
import org.example.dto.response.PaddingResponse;
import org.example.services.PaddingService;

@Validated
@RestController
@RequestMapping("/enrichment")
@RequiredArgsConstructor
public class PaddingControllerImpl implements PaddingController {

  private final PaddingService enrichmentService;

  @Override
  @GetMapping("/findAll")
  @ResponseStatus(value = HttpStatus.OK)
  public Iterable<PaddingResponse> getAllEnrichmentRequests() {
    return enrichmentService.getAllEnrichmentRequests();
  }

  @Override
  @GetMapping("/findAll/{id}")
  @ResponseStatus(value = HttpStatus.OK)
  public Iterable<PaddingResponse> getAllEnrichmentRequestsByEnrichmentRequestId(
      @PathVariable("id") long id) {
    return enrichmentService.getAllEnrichmentRequestsByEnrichmentRequestId(id);
  }

  @Override
  @GetMapping("/find/{enrichmentId}/{ruleId}")
  @ResponseStatus(value = HttpStatus.OK)
  public PaddingResponse getEnrichmentRequestById(
      @PathVariable("enrichmentId") long enrichmentId,
      @PathVariable("ruleId") long ruleId) {
    return enrichmentService.getEnrichmentRequestById(enrichmentId, ruleId);
  }

  @Override
  @DeleteMapping("/delete")
  @ResponseStatus(value = HttpStatus.OK)
  public void deleteEnrichmentRequest() {
    enrichmentService.deleteEnrichmentRequest();
  }

  @Override
  @DeleteMapping("/delete/{enrichmentId}/{ruleId}")
  @ResponseStatus(value = HttpStatus.OK)
  public void deleteEnrichmentRequestById(
      @PathVariable("enrichmentId") long enrichmentId,
      @PathVariable("ruleId") long ruleId) {
    enrichmentService.deleteEnrichmentRequestById(enrichmentId, ruleId);
  }

  @Override
  @PostMapping("/save")
  @ResponseStatus(value = HttpStatus.CREATED)
  public void save(@RequestBody @Valid PaddingRequest enrichment) {
    enrichmentService.save(enrichment);
  }
}
