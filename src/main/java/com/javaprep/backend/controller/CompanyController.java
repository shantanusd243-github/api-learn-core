package com.javaprep.backend.controller;

import com.javaprep.backend.dto.common.TaxonomyItemResponse;
import com.javaprep.backend.dto.company.CompanyRequest;
import com.javaprep.backend.service.TaxonomyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final TaxonomyService taxonomyService;

    @GetMapping
    public ResponseEntity<List<TaxonomyItemResponse>> list() {
        return ResponseEntity.ok(taxonomyService.listCompanies());
    }

    @PostMapping
    public ResponseEntity<TaxonomyItemResponse> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taxonomyService.createCompany(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxonomyItemResponse> update(@PathVariable String id,
                                                         @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(taxonomyService.updateCompany(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        taxonomyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
