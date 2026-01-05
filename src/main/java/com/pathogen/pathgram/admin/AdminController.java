package com.pathogen.pathgram.admin;

import com.pathogen.pathgram.model.EventLog;
import com.pathogen.pathgram.repository.EventLogRepository;
import com.pathogen.pathgram.service.Hl7Processor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final EventLogRepository eventLogRepository;
    private final Hl7Processor hl7Processor;

    public AdminController(EventLogRepository eventLogRepository, Hl7Processor hl7Processor) {
        this.eventLogRepository = eventLogRepository;
        this.hl7Processor = hl7Processor;
    }

    @PostMapping("/replay")
    public ResponseEntity<?> replay(@RequestParam UUID eventId) {
        Optional<EventLog> ev = eventLogRepository.findById(eventId);
        if (ev.isEmpty()) return ResponseEntity.notFound().build();
        EventLog log = ev.get();
        // re-send raw HL7
        String ack = hl7Processor.process(log.getRawMessage(), "replay");
        // persist outbound event log
        EventLog out = new EventLog();
        out.setEventId(UUID.randomUUID());
        out.setRawMessage(ack);
        out.setDirection(EventLog.Direction.outbound);
        out.setAckStatus("SENT");
        out.setCreatedBy("admin-replay");
        eventLogRepository.save(out);
        return ResponseEntity.accepted().build();
    }
}

