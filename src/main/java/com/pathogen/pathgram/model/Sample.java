package com.pathogen.pathgram.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Sample")
public class Sample {

    @Id
    private UUID sampleId;

    private UUID patientId;

    private Instant receivedAt;

    private String sourceIp;

    private String status;

    private String mode;

    private Instant createdAt;

    public Sample() {}

    @PrePersist
    public void prePersist() {
        if (sampleId == null) sampleId = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getSampleId() { return sampleId; }
    public void setSampleId(UUID sampleId) { this.sampleId = sampleId; }
    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

