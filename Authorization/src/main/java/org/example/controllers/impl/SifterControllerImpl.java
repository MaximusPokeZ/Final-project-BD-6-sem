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
import org.example.controllers.SifterController;
import org.example.dto.request.SifterRequest;
import org.example.dto.response.SifterResponse;
import org.example.services.SifterService;

@Validated
@RestController
@RequestMapping("/filter")
@RequiredArgsConstructor
public class SifterControllerImpl implements SifterController {

  private final SifterService filterService;

  @Override
  @GetMapping("/findAll")
  @ResponseStatus(value = HttpStatus.OK)
  public Iterable<SifterResponse> getAllFilters() {
    return filterService.getAllFilters();
  }

  @Override
  @GetMapping("/findAll/{id}")
  @ResponseStatus(value = HttpStatus.OK)
  public Iterable<SifterResponse> getAllFiltersByFilterId(
      @PathVariable("id") long id) {
    return filterService.getAllFiltersByFilterId(id);
  }

  @Override
  @GetMapping("/find/{filterId}/{ruleId}")
  @ResponseStatus(value = HttpStatus.OK)
  public SifterResponse getFilterByFilterIdAndRuleId(
      @PathVariable("filterId") long filterId,
      @PathVariable("ruleId") long ruleId) {
    return filterService.getFilterByFilterIdAndRuleId(filterId, ruleId);
  }

  @Override
  @DeleteMapping("/delete")
  @ResponseStatus(value = HttpStatus.OK)
  public void deleteFilter() {
    filterService.deleteFilter();
  }

  @Override
  @DeleteMapping("/delete/{filterId}/{ruleId}")
  @ResponseStatus(value = HttpStatus.OK)
  public void deleteFilterById(
      @PathVariable("filterId") long filterId,
      @PathVariable("ruleId") long ruleId) {
    filterService.deleteFilterById(filterId, ruleId);
  }

  @Override
  @PostMapping("/save")
  @ResponseStatus(value = HttpStatus.CREATED)
  public void save(@RequestBody @Valid SifterRequest filter) {
    filterService.save(filter);
  }
}
