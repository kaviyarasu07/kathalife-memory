package com.kathalife.memory.embedding.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "journal_entry_content")
public class JournalEntryContent {

    @Id
    @Column(name = "journal_entry_id", nullable = false)
    private String journalEntryId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "full_text", nullable = false)
    private String fullText;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "language", nullable = false)
    private String language;
}
