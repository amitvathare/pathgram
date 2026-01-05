package com.pathogen.pathgram.listener;

import com.pathogen.pathgram.service.Hl7Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.netty.tcp.TcpServer;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Component
public class MllpServer {

    private final Hl7Processor hl7Processor;
    private final int port;

    public MllpServer(Hl7Processor hl7Processor, @Value("${mllp.port:5100}") int port) {
        this.hl7Processor = hl7Processor;
        this.port = port;
    }

    @PostConstruct
    public void start() {
        TcpServer.create()
                .host("0.0.0.0")
                .port(port)
                .handle((inbound, outbound) -> {
                    Flux<String> messages = inbound.receive()
                            .asByteArray()
                            .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                            .flatMapIterable(this::splitFrames);

                    return outbound.sendString(
                            messages.flatMap(msg -> {
                                // process and return framed ACK
                                String ack = hl7Processor.process(msg, "unknown");
                                String framedAck = "\u000b" + ack + "\u001c\r";
                                return Flux.just(framedAck);
                            })
                    );
                })
                .bindNow();

        System.out.println("MLLP server started on port " + port);
    }

    private Iterable<String> splitFrames(String chunk) {
        java.util.List<String> frames = new java.util.ArrayList<>();
        int idx = 0;
        while (idx < chunk.length()) {
            int sb = chunk.indexOf('\u000b', idx);
            if (sb == -1) break;
            int eb = chunk.indexOf('\u001c', sb + 1);
            if (eb == -1) break;
            String payload = chunk.substring(sb + 1, eb);
            frames.add(payload);
            idx = eb + 1;
            if (idx < chunk.length() && chunk.charAt(idx) == '\r') idx++;
        }
        return frames;
    }
}
