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
import org.example.controllers.SelectionController;
import org.example.dto.request.SelectionRequest;
import org.example.dto.response.SelectionResponse;
import org.example.services.SelectionService;

@Validated
@RestController
@RequestMapping("/deduplication")
@RequiredArgsConstructor
public class SelectionControllerImpl implements SelectionController {

  private final SelectionService service;

  @Override
  @GetMapping("/findAll")
  @ResponseStatus(value = HttpStatus.OK)
  public Iterable<SelectionResponse> getAllDeduplications() {
    return service.getAllDeduplications();
  }

  @Override
  @GetMapping("/findAll/{id}")
  @ResponseStatus(value = HttpStatus.OK)
  public Iterable<SelectionResponse> getAllDeduplicationsByDeduplicationId(
      @PathVariable("id") long id) {
    return service.getAllDeduplicationsByDeduplicationId(id);
  }

  @Override
  @GetMapping("/find/{deduplicationId}/{ruleId}")
  @ResponseStatus(value = HttpStatus.OK)
  public SelectionResponse getDeduplicationById(
      @PathVariable("deduplicationId") long deduplicationId,
      @PathVariable("ruleId") long ruleId) {
    return service.getDeduplicationById(deduplicationId, ruleId);
  }

  @Override
  @DeleteMapping("/delete")
  @ResponseStatus(value = HttpStatus.OK)
  public void deleteDeduplication() {
    service.deleteDeduplication();
  }

  @Override
  @DeleteMapping("/delete/{deduplicationId}/{ruleId}")
  @ResponseStatus(value = HttpStatus.OK)
  public void deleteDeduplicationById(
      @PathVariable("deduplicationId") long deduplicationId,
      @PathVariable("ruleId") long ruleId) {
    service.deleteDeduplicationById(deduplicationId, ruleId);
  }

  @Override
  @PostMapping("/save")
  @ResponseStatus(value = HttpStatus.CREATED)
  public void save(@RequestBody @Valid SelectionRequest deduplication) {
    service.save(deduplication);
  }
}
