package com.pathogen.pathgram.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path baseDir;

    public LocalStorageService(@Value("${storage.local.path:./storage}") String baseDir) {
        this.baseDir = Paths.get(baseDir);
    }

    @PostConstruct
    public void init() throws Exception {
        Files.createDirectories(baseDir);
    }

    @Override
    public StorageResult store(byte[] data, String contentType, String keyHint) throws Exception {
        String name = (keyHint == null || keyHint.isBlank()) ? UUID.randomUUID().toString() : keyHint;
        // ensure unique
        String filename = name + "-" + UUID.randomUUID();
        Path target = baseDir.resolve(filename);
        try (FileOutputStream fos = new FileOutputStream(target.toFile())) {
            fos.write(data);
        }
        return new StorageResult(target.toAbsolutePath().toString(), data.length);
    }
}
