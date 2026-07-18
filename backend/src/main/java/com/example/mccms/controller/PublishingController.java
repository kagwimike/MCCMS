package com.example.mccms.controller;

import com.example.mccms.service.PublishingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/deliverables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublishingController {

    private final PublishingService publishingService;

    @PostMapping("/{id}/publish")
    public ResponseEntity<Map<String, String>> publish(@PathVariable Long id, Authentication authentication) {
        try {
            String url = publishingService.publishDeliverable(id, authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Published successfully", "url", url));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
