package org.example.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.dto.request.SifterRequest;
import org.example.dto.response.SifterResponse;

/**
 * Клиент для контролера фильтрации
 */
@FeignClient(
    name = "filterClient",
    url = "${feign.client.url.filter}"
)
public interface FilterClient {
    @GetMapping("/findAll")
    Iterable<SifterResponse> getAllFilters();

    @GetMapping("/findAll/{id}")
    Iterable<SifterResponse> getAllFiltersByFilterId(@PathVariable long id);

    @GetMapping("/find/{filterId}/{ruleId}")
    SifterResponse getFilterByFilterIdAndRuleId(@PathVariable long filterId, @PathVariable long ruleId);

    @DeleteMapping("/delete")
    void deleteFilter();

    @DeleteMapping("/delete/{filterId}/{ruleId}")
    void deleteFilterById(@PathVariable long filterId, @PathVariable long ruleId);

    @PostMapping("/save")
    ResponseEntity<SifterResponse> save(@RequestBody SifterRequest filter);
}
