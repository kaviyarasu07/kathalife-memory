package com.kathalife.memory.embedding;

import com.kathalife.memory.service.ChunkingService;
import com.kathalife.memory.service.TextChunk;
import com.kathalife.memory.consumer.dto.JournalEntrySavedEvent;
import com.kathalife.memory.embedding.client.EmbeddingClient;
import com.kathalife.memory.embedding.client.EmbeddingClientException;
import com.kathalife.memory.embedding.client.EmbeddingInputType;
import com.kathalife.memory.embedding.model.JournalEmbedding;
import com.kathalife.memory.embedding.model.JournalEntryContent;
import com.kathalife.memory.embedding.persistence.JournalEmbeddingPersistenceService;
import com.kathalife.memory.embedding.persistence.JournalEntryContentRepository;
import com.kathalife.memory.service.TextChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalEmbeddingOrchestrationService {

    private final JournalEntryContentRepository contentRepository;
    private final JournalEmbeddingPersistenceService persistenceService;
    private final ChunkingService chunkingService;
    private final EmbeddingClient embeddingClient;

    public void processJournalEntry(JournalEntrySavedEvent event) throws EmbeddingClientException {
        Optional<JournalEntryContent> existingContent = contentRepository.findByJournalEntryId(event.journalEntryId());

        if (existingContent.isPresent() && existingContent.get().getFullText().equals(event.fullText())) {
            log.info("Journal entry {} is a duplicate, skipping.", event.journalEntryId());
            return;
        }

        List<TextChunk> chunks = chunkingService.chunk(event.fullText());
        List<String> chunkTexts = chunks.stream().map(TextChunk::text).toList();
        List<float[]> embeddings = embeddingClient.embed(chunkTexts, EmbeddingInputType.SEARCH_DOCUMENT);

        JournalEntryContent content = JournalEntryContent.builder()
                .journalEntryId(event.journalEntryId())
                .userId(event.userId())
                .fullText(event.fullText())
                .activityDate(event.activityDate())
                .language("English")
                .build();

        List<JournalEmbedding> journalEmbeddings = IntStream.range(0, chunks.size())
                .mapToObj(i -> JournalEmbedding.builder()
                        .journalEntryId(event.journalEntryId())
                        .chunkText(chunks.get(i).text())
                        .chunkSequence(chunks.get(i).sequence())
                        .embeddingVector(embeddings.get(i))
                        .build())
                .toList();

        persistenceService.persistEmbeddings(content, journalEmbeddings, existingContent.isPresent());
    }
}
