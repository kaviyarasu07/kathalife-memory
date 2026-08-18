package com.kathalife.memory.search.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MemorySearchResult(
        UUID journalEntryId,
        LocalDate entryDate,
        String chunkText,
        int chunkSequence,
        double similarityScore
) {
}