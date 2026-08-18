package com.kathalife.memory.embedding.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journal_embeddings")
public class JournalEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "embedding_id", nullable = false)
    private UUID embeddingId;

    @Column(name = "journal_entry_id", nullable = false)
    private String journalEntryId;

    @Column(name = "chunk_text", nullable = false)
    private String chunkText;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1024)
    @Column(name = "embedding_vector", nullable = false)
    private float[] embeddingVector;

    @Column(name = "chunk_sequence", nullable = false)
    private Integer chunkSequence;
}
