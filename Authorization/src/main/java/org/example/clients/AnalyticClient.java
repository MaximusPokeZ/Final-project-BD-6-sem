package org.example.clients;

import jakarta.validation.Valid;
import org.example.dto.request.AnalyticRequest;
import org.example.dto.response.AnalyticResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "analyticClient",
        url = "${feign.client.url.analytic}"
)
public interface AnalyticClient {
    @GetMapping("/findAll")
    Iterable<AnalyticResponse> getAllAnalytics();

    @GetMapping("/findAll/{id}")
    Iterable<AnalyticResponse> getAllAnalyticsByServiceId(@PathVariable long id);

    @DeleteMapping("/delete")
    void deleteAllAnalytics();

    @PostMapping("/save")
    ResponseEntity<AnalyticResponse> save(@RequestBody @Valid AnalyticRequest analyticRequest);
}
