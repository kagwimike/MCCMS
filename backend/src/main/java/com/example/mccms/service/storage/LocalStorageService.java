package com.example.mccms.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    private final Path rootLocation;

    public LocalStorageService(@Value("${storage.local.path:./uploads}") String path) {
        this.rootLocation = Paths.get(path);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new IOException("Failed to store empty file " + filename);
        }
        
        String extension = filename.substring(filename.lastIndexOf("."));
        String storedName = UUID.randomUUID().toString() + extension;

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, this.rootLocation.resolve(storedName),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        
        return "/uploads/" + storedName;
    }

    @Override
    public void delete(String filePath) {
        try {
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            Files.deleteIfExists(this.rootLocation.resolve(filename));
        } catch (IOException e) {
            System.err.println("Could not delete file: " + filePath);
        }
    }
}
