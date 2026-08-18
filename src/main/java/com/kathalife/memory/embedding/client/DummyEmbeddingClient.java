package com.kathalife.memory.embedding.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "embedding.provider", havingValue = "dummy", matchIfMissing = true)
public class DummyEmbeddingClient implements EmbeddingClient {
    @Override
    public List<float[]> embed(List<String> texts, EmbeddingInputType inputType) throws EmbeddingClientException {
        return texts.stream().map(text -> new float[]{1.0f, 2.0f, 3.0f}).toList();
    }
}
