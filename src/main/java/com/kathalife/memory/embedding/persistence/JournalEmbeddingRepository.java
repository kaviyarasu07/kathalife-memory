package com.kathalife.memory.embedding.persistence;

import com.kathalife.memory.embedding.model.JournalEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JournalEmbeddingRepository extends JpaRepository<JournalEmbedding, UUID> {
    void deleteByJournalEntryId(String journalEntryId);
}
