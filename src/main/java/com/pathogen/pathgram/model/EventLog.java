package com.pathogen.pathgram.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_log")
public class EventLog {

    @Id
    private UUID eventId;

    private UUID sampleId;

    // Use explicit TEXT column to match the database schema and avoid CLOB/OID mapping mismatches
    @Column(name = "raw_message", columnDefinition = "text")
    private String rawMessage;

    @Enumerated(EnumType.STRING)
    private Direction direction = Direction.inbound;

    private String ackStatus;

    private Instant processedAt;

    private String createdBy;

    public EventLog() {
    }

    @PrePersist
    public void prePersist() {
        if (eventId == null) eventId = UUID.randomUUID();
        if (processedAt == null) processedAt = Instant.now();
    }

    // getters and setters

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public UUID getSampleId() { return sampleId; }
    public void setSampleId(UUID sampleId) { this.sampleId = sampleId; }
    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public String getAckStatus() { return ackStatus; }
    public void setAckStatus(String ackStatus) { this.ackStatus = ackStatus; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public enum Direction { inbound, outbound }
}
