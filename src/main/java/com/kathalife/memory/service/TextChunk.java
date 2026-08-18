package com.kathalife.memory.service;

public record TextChunk(
    String text,
    int sequence,
    int tokenCount
) {}
