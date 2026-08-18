package com.kathalife.memory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ChunkingServiceTest {

    private ChunkingService chunkingService;

    @BeforeEach
    void setUp() {
        chunkingService = new ChunkingService();
    }

    @Test
    @DisplayName("1. Single short paragraph -> 1 chunk")
    void singleShortParagraph() {
        // Given
        String text = "This is a single, short paragraph.";

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0).text());
        assertEquals(0, chunks.get(0).sequence());
    }

    @Test
    @DisplayName("2. Two short paragraphs that merge -> 1 chunk")
    void twoShortParagraphsMerge() {
        // Given
        String para1 = "This is the first paragraph.";
        String para2 = "This is the second paragraph.";
        String text = para1 + "\n\n" + para2;

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0).text());
    }

    @Test
    @DisplayName("3. Three short paragraphs, first two merge, third is separate -> 2 chunks")
    void threeParagraphsPartialMerge() {
        // Given
        String para1 = repeatWords("word", 150);
        String para2 = repeatWords("word", 100); // para1 + para2 fits
        String para3 = repeatWords("word", 1);   // para1 + para2 + para3 does not fit
        String text = String.join("\n\n", para1, para2, para3);

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertEquals(2, chunks.size());
        assertEquals(String.join("\n\n", para1, para2), chunks.get(0).text());
        assertEquals(para3, chunks.get(1).text());
        assertEquals(0, chunks.get(0).sequence());
        assertEquals(1, chunks.get(1).sequence());
    }

    @Test
    @DisplayName("4. Oversized paragraph with clean sentence boundaries -> 2 chunks")
    void oversizedParagraphWithCleanSentences() {
        // Given
        String sentence1 = repeatWords("This is the first long sentence.", 40); // 200 tokens
        String sentence2 = repeatWords("This is the second long sentence.", 40); // 200 tokens
        String text = sentence1 + " " + sentence2;

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertEquals(2, chunks.size());
        assertTrue(chunks.get(0).text().endsWith("."));
        assertTrue(chunks.get(1).text().startsWith("This"));
        assertTrue(chunks.get(0).tokenCount() <= 250);
        assertTrue(chunks.get(1).tokenCount() <= 250);
    }

    @Test
    @DisplayName("5. Oversized paragraph with one giant sentence -> triggers hard-cut")
    void oversizedParagraphWithGiantSentence() {
        // Given
        String text = repeatWords("word", 300); // One long sentence with no punctuation

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertEquals(2, chunks.size());
        assertEquals(250, chunks.get(0).tokenCount());
        assertEquals(50, chunks.get(1).tokenCount());
        assertTrue(chunks.get(0).text().startsWith("word"));
        assertTrue(chunks.get(1).text().startsWith("word"));
    }

    @Test
    @DisplayName("6. CRITICAL: Split paragraph remainder does NOT merge with next paragraph")
    void splitRemainderDoesNotMerge() {
        // Given
        String para1 = repeatWords("word", 310); // Will be split into 250 + 60
        String para2 = repeatWords("next", 60);  // Should NOT merge with the 60-token remainder
        String text = para1 + "\n\n" + para2;

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertEquals(3, chunks.size(), "Should produce 3 separate chunks");
        assertEquals(250, chunks.get(0).tokenCount(), "First chunk is hard-cut of para1");
        assertEquals(60, chunks.get(1).tokenCount(), "Second chunk is remainder of para1");
        assertEquals(60, chunks.get(2).tokenCount(), "Third chunk is para2, unmerged");
        assertEquals(0, chunks.get(0).sequence());
        assertEquals(1, chunks.get(1).sequence());
        assertEquals(2, chunks.get(2).sequence());
    }

    @Test
    @DisplayName("7. Text with 'Dr.' abbreviation is not split incorrectly")
    void abbreviationNotSplit() {
        // Given
        String text = "Dr. Patel said the results were fine. I felt relieved.";

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0).text());
    }

    @Test
    @DisplayName("8. Unpunctuated text falls back to hard-cut")
    void unpunctuatedTextFallsBackToHardCut() {
        // Given
        String text = repeatWords("unpunctuated", 300);

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertDoesNotThrow(() -> chunkingService.chunk(text));
        assertEquals(2, chunks.size());
        assertEquals(250, chunks.get(0).tokenCount());
        assertEquals(50, chunks.get(1).tokenCount());
    }

    @Test
    @DisplayName("9. Empty and whitespace-only strings return empty list")
    void emptyAndWhitespaceInput() {
        // Given
        String emptyText = "";
        String whitespaceText = "   \n\t   ";

        // When
        List<TextChunk> emptyChunks = chunkingService.chunk(emptyText);
        List<TextChunk> whitespaceChunks = chunkingService.chunk(whitespaceText);

        // Then
        assertTrue(emptyChunks.isEmpty());
        assertTrue(whitespaceChunks.isEmpty());
    }

    @Test
    @DisplayName("10. Composite test with merge, split, and hard-cut, asserting sequence")
    void compositeTestAssertsSequence() {
        // Given
        String para1 = repeatWords("mergeA", 100);
        String para2 = repeatWords("mergeB", 100); // Merges with para1
        String para3 = repeatWords("split", 300);   // Splits into 250 + 50
        String para4 = repeatWords("final", 50);    // Does not merge with remainder of para3
        String text = String.join("\n\n", para1, para2, para3, para4);

        // When
        List<TextChunk> chunks = chunkingService.chunk(text);

        // Then
        assertEquals(4, chunks.size());

        // Chunk 0: para1 + para2 merged
        assertEquals(200, chunks.get(0).tokenCount());
        assertTrue(chunks.get(0).text().contains("mergeA"));
        assertTrue(chunks.get(0).text().contains("mergeB"));

        // Chunk 1: First part of para3 (hard-cut)
        assertEquals(250, chunks.get(1).tokenCount());
        assertTrue(chunks.get(1).text().startsWith("split"));

        // Chunk 2: Remainder of para3
        assertEquals(50, chunks.get(2).tokenCount());
        assertTrue(chunks.get(2).text().startsWith("split"));

        // Chunk 3: para4 alone
        assertEquals(50, chunks.get(3).tokenCount());
        assertTrue(chunks.get(3).text().startsWith("final"));

        // Assert sequence is strictly increasing
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).sequence(), "Sequence numbers must be strictly increasing");
        }
    }

    private String repeatWords(String word, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> word)
                .collect(Collectors.joining(" "));
    }
}
