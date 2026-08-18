package com.kathalife.memory.embedding.client;

import java.util.List;

public interface EmbeddingClient {
    List<float[]> embed(List<String> texts, EmbeddingInputType inputType) throws EmbeddingClientException;
}
