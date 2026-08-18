package com.kathalife.memory.search.service;

import com.kathalife.memory.embedding.client.CohereEmbeddingClient;
import com.kathalife.memory.search.config.RagProperties;
import com.kathalife.memory.search.dto.MemorySearchRequest;
import com.kathalife.memory.search.dto.MemorySearchResponse;
import com.kathalife.memory.search.dto.MemorySearchResult;
import com.kathalife.memory.search.repository.MemorySearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemorySearchService {

    private final CohereEmbeddingClient cohereEmbeddingClient;
    private final MemorySearchRepository memorySearchRepository;
    private final RagProperties ragProperties;

    public MemorySearchService(CohereEmbeddingClient cohereEmbeddingClient,
                               MemorySearchRepository memorySearchRepository,
                               RagProperties ragProperties) {
        this.cohereEmbeddingClient = cohereEmbeddingClient;
        this.memorySearchRepository = memorySearchRepository;
        this.ragProperties = ragProperties;
    }

    public MemorySearchResponse search(MemorySearchRequest request) {
        float[] queryEmbedding = cohereEmbeddingClient.embed(request.queryText(), "search_query");

        int topK = request.topK() != null ? request.topK() : ragProperties.topK();

        log.debug("Searching for top {} results with similarity threshold {}", topK, ragProperties.similarityThreshold());

        List<Object[]> results = memorySearchRepository.search(
                request.userId(),
                queryEmbedding,
                topK,
                ragProperties.similarityThreshold()
        );

        log.debug("Found {} candidate results.", results.size());

        List<MemorySearchResult> searchResults = results.stream()
                .map(this::mapToSearchResult)
                .collect(Collectors.toList());

        log.debug("Returning {} results.", searchResults.size());


        return new MemorySearchResponse(searchResults);
    }

    private MemorySearchResult mapToSearchResult(Object[] row) {
        UUID journalEntryId = UUID.fromString((String) row[0]);
        LocalDate entryDate = ((java.sql.Date) row[1]).toLocalDate();
        String chunkText = (String) row[2];
        int chunkSequence = (Integer) row[3];
        double distance = (Double) row[4];
        double similarity = 1 - (distance / 2);

        return new MemorySearchResult(journalEntryId, entryDate, chunkText, chunkSequence, similarity);
    }
}