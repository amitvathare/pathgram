package com.pathogen.pathgram.service;

import com.pathogen.pathgram.model.BinaryArtifact;
import com.pathogen.pathgram.model.Result;
import com.pathogen.pathgram.model.Sample;
import com.pathogen.pathgram.repository.BinaryArtifactRepository;
import com.pathogen.pathgram.repository.EventLogRepository;
import com.pathogen.pathgram.repository.ResultRepository;
import com.pathogen.pathgram.repository.SampleRepository;
import com.pathogen.pathgram.service.storage.StorageService;
import com.pathogen.pathgram.websocket.WebSocketEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class Hl7ProcessorTest {

    @Mock
    EventLogRepository eventLogRepository;
    @Mock
    SampleRepository sampleRepository;
    @Mock
    ResultRepository resultRepository;
    @Mock
    BinaryArtifactRepository binaryArtifactRepository;
    @Mock
    StorageService storageService;
    @Mock
    private WebSocketEventPublisher webSocketEventPublisher;

    @InjectMocks
    Hl7Processor hl7Processor;

    @Test
    public void testProcessStoresResultAndReturnsAck() {
        when(eventLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(sampleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(resultRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String hl7 = "MSH|^~\\&|sender|recv|fac|app|20250101000000||ORM^O01|MSGID123|P|2.3.1\r" +
                "OBX|ST|OBX01|OBX01||45|mg\r";

        String ack = hl7Processor.process(hl7, "127.0.0.1");

        assertNotNull(ack);
        assertTrue(ack.contains("MSGID123"), "ACK should contain original MSH-10");

        verify(eventLogRepository, atLeast(1)).save(any());
        verify(sampleRepository, times(1)).save(any(Sample.class));
        verify(resultRepository, times(1)).save(any(Result.class));
        verify(binaryArtifactRepository, never()).save(any(BinaryArtifact.class));
    }

    @Test
    public void testProcessStoresBinaryArtifactWhenBase64Payload() throws Exception {
        when(eventLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(sampleRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(binaryArtifactRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(storageService.store(any(), any(), any())).thenReturn(new StorageService.StorageResult("/tmp/path", 5L));

        // OBX value contains a trailing base64 payload after the last ';' per Hl7Processor.decodeBase64Payload
        String hl7 = "MSH|^~\\&|sender|recv|fac|app|20250101000000||ORM^O01|MSGID_B64|P|2.3.1\r" +
                "OBX|ED|OBXBIN|OBXBIN||meta;base64;SGVsbG8=|\r";

        String ack = hl7Processor.process(hl7, "127.0.0.1");

        assertNotNull(ack);
        assertTrue(ack.contains("MSGID_B64"));

        verify(eventLogRepository, atLeast(1)).save(any());
        verify(sampleRepository, times(1)).save(any(Sample.class));
        verify(storageService, times(1)).store(any(), eq("application/octet-stream"), any());
        verify(binaryArtifactRepository, times(1)).save(any(BinaryArtifact.class));
        verify(resultRepository, never()).save(any(Result.class));

        ArgumentCaptor<BinaryArtifact> captor = ArgumentCaptor.forClass(BinaryArtifact.class);
        verify(binaryArtifactRepository).save(captor.capture());
        BinaryArtifact saved = captor.getValue();
        assertEquals("application/octet-stream", saved.getContentType());
        assertEquals(5L, saved.getSize());
        assertNotNull(saved.getStoragePath());
    }
}
