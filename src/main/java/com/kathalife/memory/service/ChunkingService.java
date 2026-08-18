package com.kathalife.memory.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChunkingService {

    private static final int MAX_TOKENS_PER_CHUNK = 250;

    public List<TextChunk> chunk(String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return List.of();
        }

        List<TextChunk> chunks = new ArrayList<>();
        List<String> paragraphs = Arrays.stream(fullText.split("\\n\\s*\\n"))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());

        StringBuilder currentBuffer = new StringBuilder();
        int sequenceCounter = 0;

        for (String paragraph : paragraphs) {
            int paragraphTokenCount = countTokens(paragraph);

            if (paragraphTokenCount > MAX_TOKENS_PER_CHUNK) {
                if (currentBuffer.length() > 0) {
                    chunks.add(new TextChunk(currentBuffer.toString(), sequenceCounter++, countTokens(currentBuffer.toString())));
                    currentBuffer.setLength(0);
                    log.debug("Flushed buffer before splitting oversized paragraph. New chunk count: {}", chunks.size());
                }

                sequenceCounter = splitOversizedParagraph(paragraph, chunks, sequenceCounter);
                // After splitting a large paragraph, the next paragraph MUST start a new chunk.
                // The loop will naturally do this by resetting the buffer or evaluating merge conditions.
                // We ensure no merging by not carrying over any buffer content.
                currentBuffer.setLength(0); // Explicitly reset buffer after a split
                continue;
            }

            int bufferTokenCount = countTokens(currentBuffer.toString());
            if (bufferTokenCount + paragraphTokenCount <= MAX_TOKENS_PER_CHUNK) {
                if (currentBuffer.length() > 0) {
                    currentBuffer.append("\n\n");
                }
                currentBuffer.append(paragraph);
                log.debug("Merged paragraph into current buffer. Buffer token count: {}", countTokens(currentBuffer.toString()));
            } else {
                if (currentBuffer.length() > 0) {
                    chunks.add(new TextChunk(currentBuffer.toString(), sequenceCounter++, countTokens(currentBuffer.toString())));
                    log.debug("Flushed buffer because next paragraph would exceed token limit. New chunk count: {}", chunks.size());
                }
                currentBuffer.setLength(0);
                currentBuffer.append(paragraph);
            }
        }

        if (currentBuffer.length() > 0) {
            chunks.add(new TextChunk(currentBuffer.toString(), sequenceCounter, countTokens(currentBuffer.toString())));
            log.debug("Flushed final buffer. Total chunks: {}", chunks.size());
        }

        return chunks;
    }

    private int splitOversizedParagraph(String paragraph, List<TextChunk> chunks, int sequenceCounter) {
        log.debug("Splitting oversized paragraph with token count: {}", countTokens(paragraph));
        BreakIterator sentenceIterator = BreakIterator.getSentenceInstance(Locale.US);
        sentenceIterator.setText(paragraph);

        StringBuilder subBuffer = new StringBuilder();
        int start = sentenceIterator.first();
        for (int end = sentenceIterator.next(); end != BreakIterator.DONE; start = end, end = sentenceIterator.next()) {
            String sentence = paragraph.substring(start, end).trim();
            if (sentence.isEmpty()) {
                continue;
            }

            int sentenceTokenCount = countTokens(sentence);
            if (sentenceTokenCount > MAX_TOKENS_PER_CHUNK) {
                // First, flush whatever was in the sub-buffer before this giant sentence
                if (subBuffer.length() > 0) {
                    chunks.add(new TextChunk(subBuffer.toString(), sequenceCounter++, countTokens(subBuffer.toString())));
                    subBuffer.setLength(0);
                }
                // Then, handle the giant sentence with hard cuts
                sequenceCounter = hardCutSentence(sentence, chunks, sequenceCounter);
                continue; // Move to the next sentence
            }

            int subBufferTokenCount = countTokens(subBuffer.toString());
            if (subBufferTokenCount + sentenceTokenCount <= MAX_TOKENS_PER_CHUNK) {
                if (subBuffer.length() > 0) {
                    subBuffer.append(" ");
                }
                subBuffer.append(sentence);
            } else {
                if (subBuffer.length() > 0) {
                    chunks.add(new TextChunk(subBuffer.toString(), sequenceCounter++, countTokens(subBuffer.toString())));
                }
                subBuffer.setLength(0);
                subBuffer.append(sentence);
            }
        }

        if (subBuffer.length() > 0) {
            chunks.add(new TextChunk(subBuffer.toString(), sequenceCounter++, countTokens(subBuffer.toString())));
        }
        return sequenceCounter;
    }

    private int hardCutSentence(String sentence, List<TextChunk> chunks, int sequenceCounter) {
        log.warn("Sentence exceeds token limit ({} tokens). Applying hard cut fallback.", countTokens(sentence));
        List<String> words = Arrays.asList(sentence.split("\\s+"));
        StringBuilder chunkBuilder = new StringBuilder();
        int currentTokens = 0;

        for (String word : words) {
            if (currentTokens + 1 > MAX_TOKENS_PER_CHUNK) {
                chunks.add(new TextChunk(chunkBuilder.toString().trim(), sequenceCounter++, countTokens(chunkBuilder.toString().trim())));
                chunkBuilder.setLength(0);
                currentTokens = 0;
            }
            if (chunkBuilder.length() > 0) {
                chunkBuilder.append(" ");
            }
            chunkBuilder.append(word);
            currentTokens++;
        }

        if (chunkBuilder.length() > 0) {
            chunks.add(new TextChunk(chunkBuilder.toString().trim(), sequenceCounter++, countTokens(chunkBuilder.toString().trim())));
        }
        return sequenceCounter;
    }

    private int countTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
