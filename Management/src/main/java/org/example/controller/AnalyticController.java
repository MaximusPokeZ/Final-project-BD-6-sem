package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.model.Analytic;
import org.example.service.AnalyticService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("analytic")
@AllArgsConstructor
public class AnalyticController {
    private final AnalyticService service;

    @GetMapping("/findAll")
    @Operation(summary = "Получить информацию о всех правилах aналитики в БД")
    public Iterable<Analytic> getAllAnalytics() {
        return service.getAll();
    }

    @GetMapping("/findAll/{id}")
    @Operation(summary = "Получить информацию о всех правилах аналитики в БД по service id")
    public Iterable<Analytic> getAllAnalyticsByServiceId(@PathVariable long id) {
        return service.getAnalyticsByServiceId(id);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Удалить информацию о всех правилах аналитики")
    public void deleteAllAnalytics() {
        service.deleteAll();
    }

    @PostMapping("/save")
    @Operation(summary = "Создать правило аналитики")
    public ResponseEntity<Analytic> save(@RequestBody @Valid Analytic analytic) {
        Analytic savedAnalytic = service.saveAnalytic(analytic);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAnalytic);
    }
}
