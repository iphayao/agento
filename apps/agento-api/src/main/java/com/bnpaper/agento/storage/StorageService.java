package com.bnpaper.agento.storage;

public interface StorageService {
    String store(String filename, byte[] content);
    byte[] retrieve(String fileUrl);
}
