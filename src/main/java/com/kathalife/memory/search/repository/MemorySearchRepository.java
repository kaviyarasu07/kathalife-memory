package com.kathalife.memory.search.repository;

import com.kathalife.memory.search.model.MemorySearchResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MemorySearchRepository extends JpaRepository<MemorySearchResultEntity, String> {

    @Query(value = """
            SELECT
                jec.journal_entry_id as journalEntryId,
                jec.activity_date as entryDate,
                je.chunk_text as chunkText,
                je.chunk_sequence as chunkSequence,
                je.embedding_vector <=> CAST(:queryVector AS vector) AS distance
            FROM journal_embeddings je
            JOIN journal_entry_content jec ON je.journal_entry_id = jec.journal_entry_id
            WHERE jec.user_id = CAST(:userId AS TEXT)
            AND je.embedding_vector <=> CAST(:queryVector AS vector) < :similarityThreshold
            ORDER BY distance
            LIMIT :topK
            """, nativeQuery = true)
    List<Object[]> search(
            @Param("userId") UUID userId,
            @Param("queryVector") float[] queryVector,
            @Param("topK") int topK,
            @Param("similarityThreshold") double similarityThreshold
    );
}