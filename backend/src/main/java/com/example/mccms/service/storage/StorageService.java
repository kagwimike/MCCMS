package com.example.mccms.service.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    /**
     * Stores a file and returns its relative access path or URL.
     */
    String store(MultipartFile file) throws IOException;
    
    /**
     * Deletes a file from storage.
     */
    void delete(String filePath);
}
