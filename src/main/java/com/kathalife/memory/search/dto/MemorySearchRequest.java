package com.kathalife.memory.search.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record MemorySearchRequest(
        UUID userId,
        @NotBlank String queryText,
        @Positive Integer topK
) {
}