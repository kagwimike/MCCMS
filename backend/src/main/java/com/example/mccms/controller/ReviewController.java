package com.example.mccms.controller;

import com.example.mccms.dto.CommentResponse;
import com.example.mccms.dto.ReviewRequest;
import com.example.mccms.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliverables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    public ResponseEntity<Map<String, String>> submitReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        System.out.println("Received review for deliverable " + id + ": " + request);
        reviewService.submitReview(id, request, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "Review submitted successfully"));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getComments(id));
    }
}
