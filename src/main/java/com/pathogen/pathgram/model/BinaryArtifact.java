package com.pathogen.pathgram.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "BinaryArtifact")
public class BinaryArtifact {

    @Id
    private UUID artifactId;

    private UUID sampleId;

    private String type;
    private String storagePath;
    private String contentType;
    private Long size;

    private Instant createdAt;

    public BinaryArtifact() {}

    @PrePersist
    public void prePersist() {
        if (artifactId == null) artifactId = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getArtifactId() { return artifactId; }
    public void setArtifactId(UUID artifactId) { this.artifactId = artifactId; }
    public UUID getSampleId() { return sampleId; }
    public void setSampleId(UUID sampleId) { this.sampleId = sampleId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

