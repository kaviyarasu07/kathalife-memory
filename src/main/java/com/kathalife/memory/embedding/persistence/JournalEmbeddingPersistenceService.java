package com.kathalife.memory.embedding.persistence;

import com.kathalife.memory.embedding.model.JournalEmbedding;
import com.kathalife.memory.embedding.model.JournalEntryContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalEmbeddingPersistenceService {

    private final JournalEntryContentRepository contentRepository;
    private final JournalEmbeddingRepository embeddingRepository;

    @Transactional
    public void persistEmbeddings(JournalEntryContent content, List<JournalEmbedding> embeddings, boolean isEdit) {
        contentRepository.save(content);
        if (isEdit) {
            embeddingRepository.deleteByJournalEntryId(content.getJournalEntryId());
        }
        embeddingRepository.saveAll(embeddings);
    }
}
