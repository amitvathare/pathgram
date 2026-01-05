package com.pathogen.pathgram.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishNewSampleEvent(String sampleId) {
        Map<String, Object> payload = Map.of(
                "eventType", "new-sample",
                "sampleId", sampleId,
                "timestamp", Instant.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/updates", payload);
    }
}

