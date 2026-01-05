package com.pathogen.pathgram.service;

import com.pathogen.pathgram.model.BinaryArtifact;
import com.pathogen.pathgram.model.EventLog;
import com.pathogen.pathgram.model.Result;
import com.pathogen.pathgram.model.Sample;
import com.pathogen.pathgram.repository.BinaryArtifactRepository;
import com.pathogen.pathgram.repository.EventLogRepository;
import com.pathogen.pathgram.repository.ResultRepository;
import com.pathogen.pathgram.repository.SampleRepository;
import com.pathogen.pathgram.service.storage.StorageService;
import com.pathogen.pathgram.websocket.WebSocketEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class Hl7Processor {

    private final EventLogRepository eventLogRepository;
    private final SampleRepository sampleRepository;
    private final ResultRepository resultRepository;
    private final BinaryArtifactRepository binaryArtifactRepository;
    private final StorageService storageService;
    private final WebSocketEventPublisher webSocketEventPublisher;

    public Hl7Processor(EventLogRepository eventLogRepository,
                        SampleRepository sampleRepository,
                        ResultRepository resultRepository,
                        BinaryArtifactRepository binaryArtifactRepository,
                        StorageService storageService,
                        WebSocketEventPublisher webSocketEventPublisher) {
        this.eventLogRepository = eventLogRepository;
        this.sampleRepository = sampleRepository;
        this.resultRepository = resultRepository;
        this.binaryArtifactRepository = binaryArtifactRepository;
        this.storageService = storageService;
        this.webSocketEventPublisher = webSocketEventPublisher;
    }

    /**
     * Process a single HL7 message payload (without MLLP framing). Persist raw message in EventLog,
     * create a Sample, extract OBX segments into Result or BinaryArtifact and return ACK string.
     */
    public String process(String hl7Payload, String sourceIp) {
        // Persist raw message
        EventLog log = new EventLog();
        log.setEventId(UUID.randomUUID());
        log.setRawMessage(hl7Payload);
        log.setDirection(EventLog.Direction.inbound);
        log.setProcessedAt(Instant.now());
        log.setCreatedBy(sourceIp);
        eventLogRepository.save(log);

        String msh10 = extractMsh10(hl7Payload);

        // Create sample (basic mapping; in real system map from OBR to sample)
        Sample sample = new Sample();
        sample.setSampleId(UUID.randomUUID());
        sample.setReceivedAt(Instant.now());
        sample.setSourceIp(sourceIp);
        sample.setStatus("RECEIVED");
        sampleRepository.save(sample);

        // publish websocket event for real-time UI
        try {
            webSocketEventPublisher.publishNewSampleEvent(sample.getSampleId().toString());
        } catch (Exception ignored) {}

        // parse OBX segments
        List<String> lines = List.of(hl7Payload.split("\\r|\\n"));
        for (String line : lines) {
            if (line.startsWith("OBX")) {
                String[] fields = line.split("\\|", -1);
                String valueType = fields.length > 1 ? fields[1] : ""; // OBX-2
                String obxId = fields.length > 2 ? fields[2] : ""; // OBX-3
                String value = fields.length > 4 ? fields[4] : ""; // OBX-5

                if (looksLikeBase64Payload(value)) {
                    try {
                        byte[] decoded = decodeBase64Payload(value);
                        StorageService.StorageResult res = storageService.store(decoded, "application/octet-stream", msh10 + "-" + obxId);

                        BinaryArtifact art = new BinaryArtifact();
                        art.setArtifactId(UUID.randomUUID());
                        art.setSampleId(sample.getSampleId());
                        art.setType(obxId);
                        art.setStoragePath(res.getPath());
                        art.setContentType("application/octet-stream");
                        art.setSize(res.getSize());
                        binaryArtifactRepository.save(art);
                    } catch (Exception ex) {
                        // log and continue
                        System.err.println("Failed to store OBX binary payload: " + ex.getMessage());
                    }
                } else {
                    // store as Result
                    Result r = new Result();
                    r.setResultId(UUID.randomUUID());
                    r.setSampleId(sample.getSampleId());
                    r.setObxCode(obxId);
                    r.setValue(value);
                    r.setUnits(fields.length > 5 ? fields[5] : null);
                    r.setReferenceRange(fields.length > 6 ? fields[6] : null);
                    r.setFlag(fields.length > 7 ? fields[7] : null);
                    resultRepository.save(r);
                }
            }
        }

        // Build minimal ACK message (MSA-1=AA)
        StringBuilder ack = new StringBuilder();
        ack.append("MSH|^~\\&|pathgram|||||")
                .append(timeStamp())
                .append("||ACK^R01|")
                .append(UUID.randomUUID())
                .append("|P|2.3.1\r");
        ack.append("MSA|AA|").append(msh10).append("\r");

        // update event log ack status
        log.setAckStatus("AA");
        eventLogRepository.save(log);

        return ack.toString();
    }

    private boolean looksLikeBase64Payload(String value) {
        if (value == null) return false;
        // heuristics: contains 'base64' or looks like base64 string
        if (value.toLowerCase().contains("base64")) return true;
        String candidate = value.trim();
        // if it's long and contains only base64 chars
        if (candidate.length() > 40 && candidate.matches("[A-Za-z0-9+/=\\r\\n]+")) return true;
        return false;
    }

    private byte[] decodeBase64Payload(String value) {
        // attempt to find the actual base64 payload after last ';'
        int idx = value.lastIndexOf(';');
        String b64 = idx >= 0 ? value.substring(idx + 1) : value;
        b64 = b64.replaceAll("\\s+", "");
        return Base64.getDecoder().decode(b64);
    }

    private String extractMsh10(String hl7) {
        if (hl7 == null) return "";
        String[] lines = hl7.split("\\r|\\n");
        for (String l : lines) {
            if (l.startsWith("MSH")) {
                String[] fields = l.split("\\|", -1);
                if (fields.length >= 10) return fields[9];
            }
        }
        return "";
    }

    private String timeStamp() {
        return java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.Instant.now().atZone(java.time.ZoneOffset.UTC));
    }
}
