package com.pathogen.pathgram.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "result")
public class Result {

    @Id
    private UUID resultId;

    private UUID sampleId;

    private String obxCode;
    private String value;
    private String units;
    private String referenceRange;
    private String flag;

    private Instant createdAt;

    public Result() {}

    @PrePersist
    public void prePersist() {
        if (resultId == null) resultId = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getResultId() { return resultId; }
    public void setResultId(UUID resultId) { this.resultId = resultId; }
    public UUID getSampleId() { return sampleId; }
    public void setSampleId(UUID sampleId) { this.sampleId = sampleId; }
    public String getObxCode() { return obxCode; }
    public void setObxCode(String obxCode) { this.obxCode = obxCode; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getUnits() { return units; }
    public void setUnits(String units) { this.units = units; }
    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String referenceRange) { this.referenceRange = referenceRange; }
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
