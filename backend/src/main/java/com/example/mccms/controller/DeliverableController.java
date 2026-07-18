package com.example.mccms.controller;

import com.example.mccms.dto.DeliverableRequest;
import com.example.mccms.dto.DeliverableResponse;
import com.example.mccms.service.DeliverableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeliverableController {

    private final DeliverableService deliverableService;

    @PostMapping("/projects/{projectId}/deliverables")
    public ResponseEntity<DeliverableResponse> addDeliverable(
            @PathVariable Long projectId,
            @Valid @RequestBody DeliverableRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(deliverableService.addDeliverable(projectId, request, authentication.getName()));
    }

    @GetMapping("/projects/{projectId}/deliverables")
    public ResponseEntity<List<DeliverableResponse>> getDeliverables(@PathVariable Long projectId) {
        return ResponseEntity.ok(deliverableService.getDeliverablesForProject(projectId));
    }

    @PatchMapping("/deliverables/{id}/stage")
    public ResponseEntity<DeliverableResponse> updateStage(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> payload,
            Authentication authentication) {
        return ResponseEntity.ok(deliverableService.updateStage(id, payload.get("stageId"), authentication.getName()));
    }

    @PutMapping("/deliverables/{id}")
    public ResponseEntity<DeliverableResponse> updateDeliverable(
            @PathVariable Long id,
            @Valid @RequestBody DeliverableRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(deliverableService.updateDeliverable(id, request, authentication.getName()));
    }

    @DeleteMapping("/deliverables/{id}")
    public ResponseEntity<Void> deleteDeliverable(@PathVariable Long id, Authentication authentication) {
        deliverableService.deleteDeliverable(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
