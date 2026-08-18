package com.kathalife.memory.embedding.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Service
@ConditionalOnProperty(name = "embedding.provider", havingValue = "cohere")
public class CohereEmbeddingClient implements EmbeddingClient {

    private static final Logger logger = LoggerFactory.getLogger(CohereEmbeddingClient.class);

    private final RestClient restClient;
    private static final int MAX_RETRIES = 2;
    private static final long INITIAL_BACKOFF_MS = 500;


        public CohereEmbeddingClient(@Value("${cohere.api-key}") String cohereApiKey) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);

        this.restClient = RestClient.builder()
                .baseUrl("https://api.cohere.com/v2")
                .defaultHeader("Authorization", "Bearer " + cohereApiKey)
                .requestFactory(requestFactory)
                .build();
    }

    private record CohereEmbeddingRequest(
            List<String> texts,
            String model,
            @JsonProperty("input_type") String inputType,
            @JsonProperty("output_dimension") int outputDimension
    ) {}
    private record CohereEmbeddingResponse(EmbeddingsByType embeddings) {}
    private record EmbeddingsByType(@JsonProperty("float") List<float[]> floatEmbeddings) {}

    @Override
    public List<float[]> embed(List<String> texts, EmbeddingInputType inputType) throws EmbeddingClientException {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        CohereEmbeddingRequest request = new CohereEmbeddingRequest(
                texts,
                "embed-v4.0",
                inputType.toString().toLowerCase(),
                1024
        );

        int attempt = 0;
        long backoff = INITIAL_BACKOFF_MS;

        while (true) {
            try {
                CohereEmbeddingResponse response = restClient.post()
                        .uri("/embed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(CohereEmbeddingResponse.class);

                if (response != null) {
                    return response.embeddings().floatEmbeddings();
                } else {
                    throw new EmbeddingClientException("No embeddings received from Cohere");
                }

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatusCode.valueOf(400)) {
                    logger.error("Bad request to Cohere API. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                    throw new EmbeddingClientException("Bad request to Cohere API", e);
                }
                if (e.getStatusCode() == HttpStatusCode.valueOf(401)) {
                    logger.error("Unauthorized. Check Cohere API Key. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                    throw new EmbeddingClientException("Invalid Cohere API Key", e);
                }
                if (e.getStatusCode() == HttpStatusCode.valueOf(429)) {
                    if (attempt < MAX_RETRIES) {
                        logger.warn("Cohere API rate limit hit. Retrying in {}ms. Attempt {}/{}", backoff, attempt + 1, MAX_RETRIES);
                        sleep(backoff);
                        attempt++;
                        backoff *= 3;
                    } else {
                        logger.error("Cohere API rate limit exceeded after {} retries.", MAX_RETRIES);
                        throw new EmbeddingClientException("Cohere API rate limit exceeded", e);
                    }
                } else {
                     throw new EmbeddingClientException("Unhandled HTTP client error", e);
                }
            } catch (HttpServerErrorException | ResourceAccessException e) {
                if (attempt < MAX_RETRIES) {
                    logger.warn("Transient error calling Cohere API. Retrying in {}ms. Attempt {}/{}. Error: {}", backoff, attempt + 1, MAX_RETRIES, e.getMessage());
                    sleep(backoff);
                    attempt++;
                    backoff *= 3;
                } else {
                    logger.error("Failed to call Cohere API after {} retries.", MAX_RETRIES, e);
                    throw new EmbeddingClientException("Failed to get embeddings from Cohere after multiple retries", e);
                }
            } catch (Exception e) {
                logger.error("An unexpected error occurred while calling Cohere API", e);
                throw new EmbeddingClientException("An unexpected error occurred", e);
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
