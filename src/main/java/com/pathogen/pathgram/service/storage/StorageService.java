package com.pathogen.pathgram.service.storage;

public interface StorageService {
    StorageResult store(byte[] data, String contentType, String keyHint) throws Exception;

    public static final class StorageResult {
        private final String path;
        private final long size;

        public StorageResult(String path, long size) {
            this.path = path;
            this.size = size;
        }

        public String getPath() { return path; }
        public long getSize() { return size; }
    }
}

