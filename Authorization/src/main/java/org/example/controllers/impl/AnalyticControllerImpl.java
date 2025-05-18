package org.example.controllers.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.controllers.AnalyticController;
import org.example.dto.request.AnalyticRequest;
import org.example.dto.response.AnalyticResponse;
import org.example.services.AnalyticService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/analytic")
@RequiredArgsConstructor
public class AnalyticControllerImpl implements AnalyticController {
    private final AnalyticService analyticService;


    @Override
    @GetMapping("/findAll")
    @ResponseStatus(value = HttpStatus.OK)
    public Iterable<AnalyticResponse> getAllAnalyticRequests() {
        return analyticService.getAllAnalyticRequests();
    }

    @Override
    @GetMapping("/findAll/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public Iterable<AnalyticResponse> getAllAnalyticRequestsByServiceId(@PathVariable("id") long id) {
        return analyticService.getAllAnalyticRequestsByServiceId(id);
    }

    @Override
    @DeleteMapping("/delete")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteAnalyticRequest() {
        analyticService.deleteAnalyticRequest();
    }

    @Override
    @PostMapping("/save")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void save(@RequestBody @Valid AnalyticRequest analyticRequest) {
        analyticService.save(analyticRequest);
    }
}
