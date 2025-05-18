package org.example.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.example.dto.request.SelectionRequest;
import org.example.dto.response.SelectionResponse;

/**
 * Клиент для контролера дедубликации
 */
@FeignClient(
    name = "deduplicationClient",
    url = "${feign.client.url.deduplication}"
)
public interface DeduplicationClient {
    @GetMapping("/findAll")
    Iterable<SelectionResponse> getAllDeduplications();

    @GetMapping("/findAll/{id}")
    Iterable<SelectionResponse> getAllDeduplicationsByDeduplicationId(@PathVariable long id);

    @GetMapping("/find/{deduplicationId}/{ruleId}")
    SelectionResponse getDeduplicationById(@PathVariable long deduplicationId, @PathVariable long ruleId);

    @DeleteMapping("/delete")
    void deleteDeduplication() ;

    @DeleteMapping("/delete/{deduplicationId}/{ruleId}")
    void deleteDeduplicationById(@PathVariable long deduplicationId, @PathVariable long ruleId);

    @PostMapping("/save")
    @ResponseStatus(value = HttpStatus.CREATED)
    void save(@RequestBody SelectionRequest deduplication);
}
