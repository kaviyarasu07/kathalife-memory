package com.kathalife.memory.search.model;

import jakarta.persistence.Id;

import java.time.LocalDate;

public class MemorySearchResultEntity {

    @Id
    private String journalEntryId;
    private LocalDate entryDate;
    private String chunkText;
    private int chunkSequence;
    private double distance;

    public MemorySearchResultEntity(String journalEntryId, LocalDate entryDate, String chunkText, int chunkSequence, double distance) {
        this.journalEntryId = journalEntryId;
        this.entryDate = entryDate;
        this.chunkText = chunkText;
        this.chunkSequence = chunkSequence;
        this.distance = distance;
    }

    public String getJournalEntryId() {
        return journalEntryId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public String getChunkText() {
        return chunkText;
    }

    public int getChunkSequence() {
        return chunkSequence;
    }

    public double getDistance() {
        return distance;
    }
}