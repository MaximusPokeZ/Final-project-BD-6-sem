package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.model.Filter;
import org.example.service.FilterService;



@RestController
@RequestMapping("filter")
@AllArgsConstructor
@Slf4j
public class FilterController {

    private final FilterService service;

    @GetMapping("/findAll")
    @Operation(summary = "Получить информацию о всех фильтрах в БД")
    public Iterable<Filter> getAllFilters() {
        log.info("fiter rules: {}", service.getAllFilters());
        return service.getAllFilters();
    }

    @GetMapping("/findAll/{id}")
    @Operation(summary = "Получить информацию о всех фильтрах в БД по filter id")
    public Iterable<Filter> getAllFiltersByFilterId(@PathVariable long id) {
        return service.getAllFiltersByFilterId(id);
    }

    @GetMapping("/find/{filterId}/{ruleId}")
    @Operation(summary = "Получить информацию о фильтре по filter id и rule id")
    public Filter getFilterByFilterIdAndRuleId(@PathVariable long filterId, @PathVariable long ruleId) {
        return service.getFilterByFilterIdAndRuleId(filterId, ruleId);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить информацию о всех фильтрах")
    public void deleteFilter() {
        service.deleteAllFilters();

    }

    @DeleteMapping("/delete/{filterId}/{ruleId}")
    @Operation(summary = "Удалить информацию по конкретному фильтру filter id и rule id")
    public void deleteFilterById(@PathVariable long filterId, @PathVariable long ruleId) {
        service.deleteFilterByFilterIdAndRuleID(filterId, ruleId);

    }

    @PostMapping("/save")
    @Operation(summary = "Создать фильтр")
    public ResponseEntity<Filter> save(@RequestBody @Valid Filter filter) {
        Filter savedFilter = service.saveFilter(filter);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedFilter);
    }
}
