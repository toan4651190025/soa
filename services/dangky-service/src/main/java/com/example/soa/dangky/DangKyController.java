package com.example.soa.dangky;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dangky")
public class DangKyController {
    private final DangKyService service;

    public DangKyController(DangKyService service) { this.service = service; }

    @GetMapping
    public List<ApiModels.DangKyResponse> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ApiModels.DangKyResponse findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiModels.DangKyResponse create(@RequestBody ApiModels.DangKyRequest request) { return service.create(request); }

    @PutMapping("/{id}")
    public ApiModels.DangKyResponse update(@PathVariable Long id, @RequestBody ApiModels.DangKyRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}