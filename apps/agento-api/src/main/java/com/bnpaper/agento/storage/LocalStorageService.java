package com.bnpaper.agento.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;

    @Override
    public String store(String filename, byte[] content) {
        try {
            Path dir = Paths.get(properties.getLocalPath());
            Files.createDirectories(dir);
            Path dest = dir.resolve(filename);
            Files.write(dest, content);
            log.info("Stored export file: {}", dest);
            return dest.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store export file: " + filename, e);
        }
    }

    @Override
    public byte[] retrieve(String fileUrl) {
        try {
            return Files.readAllBytes(Paths.get(fileUrl));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read export file: " + fileUrl, e);
        }
    }
}
