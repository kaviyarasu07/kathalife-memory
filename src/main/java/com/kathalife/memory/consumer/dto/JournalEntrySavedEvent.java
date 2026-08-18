package com.kathalife.memory.consumer.dto;

import java.time.LocalDate;

public record JournalEntrySavedEvent(
    String journalEntryId,
    String userId,
    String fullText,
    LocalDate activityDate,
    String language
) {}
