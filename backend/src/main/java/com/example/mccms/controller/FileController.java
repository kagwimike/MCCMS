package com.example.mccms.controller;

import com.example.mccms.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Hardened controller for media asset management.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileController {

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        System.out.println("--- [DEBUG] New Upload Request Received ---");
        if (file == null || file.isEmpty()) {
            System.err.println("[DEBUG] Upload failed: File is null or empty");
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot upload empty file"));
        }
        
        try {
            long sizeInMb = file.getSize() / (1024 * 1024);
            System.out.println("[DEBUG] Name: " + file.getOriginalFilename());
            System.out.println("[DEBUG] Type: " + file.getContentType());
            System.out.println("[DEBUG] Size: " + sizeInMb + " MB");
            
            String path = storageService.store(file);
            System.out.println("[DEBUG] Success! Stored at: " + path);
            return ResponseEntity.ok(Map.of("url", path, "status", "success"));
        } catch (IOException e) {
            System.err.println("[DEBUG] Storage failure: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal storage error: " + e.getMessage()));
        } finally {
            System.out.println("--- [DEBUG] Upload Request Processing Complete ---");
        }
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get("./uploads").resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
