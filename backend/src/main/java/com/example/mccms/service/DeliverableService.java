package com.example.mccms.service;

import com.example.mccms.dto.DeliverableRequest;
import com.example.mccms.dto.DeliverableResponse;
import com.example.mccms.model.*;
import com.example.mccms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final ProjectRepository projectRepository;
    private final PlatformRepository platformRepository;
    private final StageRepository stageRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;

    @Transactional
    public DeliverableResponse addDeliverable(Long projectId, DeliverableRequest request, String userEmail) {
        System.out.println("Adding deliverable to project " + projectId + " for user " + userEmail);
        System.out.println("Request data: " + request);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        
        // Security check: Only creator or admin
        if (!user.getRole().getName().equals(Role.ADMIN) && !project.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        Platform platform = platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new RuntimeException("Platform not found"));

        // Guardrail: Prevent duplicate platform deliverables in the same project
        boolean exists = deliverableRepository.findByProject(project).stream()
                .anyMatch(d -> d.getPlatform().getId().equals(request.getPlatformId()));
        if (exists) {
            throw new RuntimeException("This project already has a " + platform.getName() + " task.");
        }

        // Default to first stage (Raw Footage)
        Stage initialStage = stageRepository.findAll().stream()
                .filter(s -> s.getSortOrder() == 1)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Initial stage not found"));

        Deliverable deliverable = new Deliverable();
        deliverable.setProject(project);
        deliverable.setPlatform(platform);
        deliverable.setStage(initialStage);
        deliverable.setCaption(request.getCaption());
        deliverable.setMediaUrl(request.getMediaUrl());
        deliverable.setScheduledAt(request.getScheduledAt());

        Deliverable saved = deliverableRepository.save(deliverable);
        auditService.log(user, "ADD", "DELIVERABLE", "SUCCESS: " + platform.getName());

        return mapToResponse(saved);
    }

    public List<DeliverableResponse> getDeliverablesForProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        return deliverableRepository.findByProject(project).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliverableResponse updateStage(Long deliverableId, Integer stageId, String userEmail) {
        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));
        
        Stage newStage = stageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Stage not found"));

        User user = userRepository.findByEmail(userEmail).orElseThrow();

        deliverable.setStage(newStage);
        deliverable.setStageUpdatedAt(LocalDateTime.now());
        
        Deliverable saved = deliverableRepository.save(deliverable);
        auditService.log(user, "UPDATE_STAGE", "DELIVERABLE", "STAGE: " + newStage.getName());

        return mapToResponse(saved);
    }

    @Transactional
    public DeliverableResponse updateDeliverable(Long id, DeliverableRequest request, String userEmail) {
        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        validateOwnership(deliverable, user);

        deliverable.setCaption(request.getCaption());
        deliverable.setMediaUrl(request.getMediaUrl());
        deliverable.setScheduledAt(request.getScheduledAt());

        Deliverable saved = deliverableRepository.save(deliverable);
        auditService.log(user, "UPDATE", "DELIVERABLE", "SUCCESS: " + id);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteDeliverable(Long id, String userEmail) {
        Deliverable deliverable = deliverableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        validateOwnership(deliverable, user);

        deliverableRepository.delete(deliverable);
        auditService.log(user, "DELETE", "DELIVERABLE", "SUCCESS: " + id);
    }

    private void validateOwnership(Deliverable deliverable, User user) {
        if (!user.getRole().getName().equals(Role.ADMIN) && 
            !deliverable.getProject().getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You do not own this content");
        }
    }

    private DeliverableResponse mapToResponse(Deliverable d) {
        return new DeliverableResponse(
                d.getId(),
                d.getPlatform().getName(),
                d.getStage().getName(),
                d.getStage().getSortOrder(),
                d.getCaption(),
                d.getMediaUrl(),
                d.getStatus(),
                d.getScheduledAt(),
                d.getStageUpdatedAt()
        );
    }
}
