package com.pathogen.pathgram.listener;

import com.pathogen.pathgram.service.Hl7Processor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class MllpServerTest {

    @Test
    public void testSplitFramesSingleFrame() throws Exception {
        Hl7Processor proc = mock(Hl7Processor.class);
        MllpServer server = new MllpServer(proc, 5100);

        Method m = MllpServer.class.getDeclaredMethod("splitFrames", String.class);
        m.setAccessible(true);

        String payload = "\u000bMSH|^~\\&|a|b|c\u001c\r";
        @SuppressWarnings("unchecked")
        Iterable<String> frames = (Iterable<String>) m.invoke(server, payload);

        List<String> list = new ArrayList<>();
        for (String f : frames) list.add(f);

        assertEquals(1, list.size());
        assertEquals("MSH|^~\\&|a|b|c", list.get(0));
    }

    @Test
    public void testSplitFramesMultipleFramesAndTrailingChars() throws Exception {
        Hl7Processor proc = mock(Hl7Processor.class);
        MllpServer server = new MllpServer(proc, 5100);

        Method m = MllpServer.class.getDeclaredMethod("splitFrames", String.class);
        m.setAccessible(true);

        String payload = "\u000bMSG1|A\u001c\r\u000bMSG2|B\u001c\rgarbage";
        @SuppressWarnings("unchecked")
        Iterable<String> frames = (Iterable<String>) m.invoke(server, payload);

        List<String> list = new ArrayList<>();
        for (String f : frames) list.add(f);

        assertEquals(2, list.size());
        assertEquals("MSG1|A", list.get(0));
        assertEquals("MSG2|B", list.get(1));
    }

    @Test
    public void testSplitFramesIncompleteFrameIgnored() throws Exception {
        Hl7Processor proc = mock(Hl7Processor.class);
        MllpServer server = new MllpServer(proc, 5100);

        Method m = MllpServer.class.getDeclaredMethod("splitFrames", String.class);
        m.setAccessible(true);

        String payload = "prefix\u000bINCOMPLETE_NO_END";
        @SuppressWarnings("unchecked")
        Iterable<String> frames = (Iterable<String>) m.invoke(server, payload);

        List<String> list = new ArrayList<>();
        for (String f : frames) list.add(f);

        assertEquals(0, list.size());
    }
}
