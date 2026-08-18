package com.kathalife.memory.search.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kathalife.rag")
public record RagProperties(
        Integer topK,
        Double similarityThreshold
) {
    public RagProperties {
        if (topK == null) {
            topK = 5;
        }
        if (similarityThreshold == null) {
            similarityThreshold = 0.5;
        }
    }
}