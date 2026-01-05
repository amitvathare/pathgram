package com.pathogen.pathgram.api;

import com.pathogen.pathgram.model.Sample;
import com.pathogen.pathgram.repository.SampleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/samples")
public class SamplesController {

    private final SampleRepository sampleRepository;

    public SamplesController(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Sample>> list(@RequestParam(required = false) String status,
                                             @RequestParam(required = false) String mode,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "50") int size) {
        // Simple implementation: ignore filters and paging for now
        List<Sample> all = sampleRepository.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sample> get(@PathVariable UUID id) {
        return sampleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/retransmit")
    public ResponseEntity<?> retransmit(@PathVariable UUID id) {
        // Placeholder: admin retransmit logic to re-send raw HL7 to LIS should be implemented here
        if (sampleRepository.existsById(id)) {
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.notFound().build();
    }
}

