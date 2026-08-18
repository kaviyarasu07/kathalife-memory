package com.kathalife.memory.consumer;

import com.kathalife.memory.consumer.dto.JournalEntrySavedEvent;
import com.kathalife.memory.embedding.JournalEmbeddingOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JournalEntryConsumer {

    private final JournalEmbeddingOrchestrationService orchestrationService;

    @KafkaListener(topics = "journal.entry.saved", groupId = "kathalife-memory-embedding-consumer")
    public void listen(JournalEntrySavedEvent event, Acknowledgment ack) {
        try {
            log.info("Processing journal entry: {}", event.journalEntryId());
            orchestrationService.processJournalEntry(event);
            ack.acknowledge();
            log.info("Successfully processed journal entry: {}", event.journalEntryId());
        } catch (Exception e) {
            log.error("Error processing journal entry: " + event.journalEntryId(), e);
            throw new RuntimeException(e);
        }
    }
}
