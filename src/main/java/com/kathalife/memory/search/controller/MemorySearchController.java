package com.kathalife.memory.search.controller;

import com.kathalife.memory.search.dto.MemorySearchRequest;
import com.kathalife.memory.search.dto.MemorySearchResponse;
import com.kathalife.memory.search.service.MemorySearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/memory")
public class MemorySearchController {

    private final MemorySearchService memorySearchService;

    public MemorySearchController(MemorySearchService memorySearchService) {
        this.memorySearchService = memorySearchService;
    }

    @PostMapping("/search")
    public MemorySearchResponse search(@Valid @RequestBody MemorySearchRequest request) {
        return memorySearchService.search(request);
    }
}