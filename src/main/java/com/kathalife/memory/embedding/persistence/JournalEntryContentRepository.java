package com.kathalife.memory.embedding.persistence;

import com.kathalife.memory.embedding.model.JournalEntryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JournalEntryContentRepository extends JpaRepository<JournalEntryContent, String> {
    Optional<JournalEntryContent> findByJournalEntryId(String journalEntryId);
}
