package com.example.mccms.service;

import com.example.mccms.dto.CommentResponse;
import com.example.mccms.dto.ReviewRequest;
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
public class ReviewService {

    private final DeliverableRepository deliverableRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final StageRepository stageRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional
    public void submitReview(Long deliverableId, ReviewRequest request, String userEmail) {
        System.out.println("[DEBUG-REVIEW] Starting review submission for ID: " + deliverableId);
        
        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));
        
        User reviewer = userRepository.findByEmail(userEmail).orElseThrow();

        // 1. Save Comment
        Comment comment = new Comment();
        comment.setDeliverable(deliverable);
        comment.setReviewer(reviewer);
        comment.setText(request.getText());
        comment.setDecision(request.getDecision());
        commentRepository.save(comment);

        // 2. Update Deliverable Status
        deliverable.setStatus(request.getDecision());
        deliverableRepository.save(deliverable);
        System.out.println("[DEBUG-REVIEW] Status updated to: " + request.getDecision());

        // 2.5 Notify Creator
        try {
            String message = String.format("Deliverable '%s' for project '%s' has been %s by %s.", 
                                           deliverable.getPlatform().getName(), 
                                           deliverable.getProject().getTitle(), 
                                           request.getDecision().toLowerCase(),
                                           reviewer.getName());
            notificationService.createNotification(deliverable.getProject().getCreator(), deliverable, "REVIEW_UPDATE", message);
            System.out.println("[DEBUG-REVIEW] Notification created for: " + deliverable.getProject().getCreator().getEmail());
        } catch (Exception e) {
            System.err.println("[DEBUG-REVIEW] Notification failed: " + e.getMessage());
        }

        auditService.log(reviewer, "REVIEW_SUBMITTED", "DELIVERABLE", request.getDecision());

        // 3. Trigger Smart Transition
        handleSmartTransitions(deliverable, reviewer);
    }

    private void handleSmartTransitions(Deliverable deliverable, User user) {
        // Updated logic for the 14-stage orchestration pipeline:
        // Trigger: YouTube (Platform) reaches Final Export (Sort Order 12) AND is Approved.
        if (deliverable.getPlatform().getName().equalsIgnoreCase("YouTube") && 
            deliverable.getStatus().equals("APPROVED") && 
            deliverable.getStage().getSortOrder() >= 12) {
            
            System.out.println("[SMART-ORCHESTRATOR] YouTube approved for export. Pulling short clips forward...");

            List<Deliverable> siblings = deliverableRepository.findByProject(deliverable.getProject());
            
            // Destination: Move TikTok/Instagram to "Short Clips Creation" (Sort Order 10)
            Stage shortClipPrep = stageRepository.findAll().stream()
                    .filter(s -> s.getSortOrder() == 10)
                    .findFirst()
                    .orElse(null);

            if (shortClipPrep != null) {
                for (Deliverable sib : siblings) {
                    // Only pull forward if it hasn't reached the prep stage yet
                    if ((sib.getPlatform().getName().equalsIgnoreCase("TikTok") || sib.getPlatform().getName().equalsIgnoreCase("Instagram"))
                        && sib.getStage().getSortOrder() < 10) {
                        
                        sib.setStage(shortClipPrep);
                        sib.setStageUpdatedAt(LocalDateTime.now());
                        deliverableRepository.save(sib);
                        
                        // 🔔 Notify Creator about the automatic move
                        String autoMsg = String.format("Auto-Pipeline: %s has been automatically moved to '%s' because YouTube was approved.", 
                                                       sib.getPlatform().getName(), 
                                                       shortClipPrep.getName());
                        notificationService.createNotification(sib.getProject().getCreator(), sib, "SYSTEM", autoMsg);

                        auditService.log(user, "SMART_TRANSITION", "DELIVERABLE", 
                                sib.getPlatform().getName() + " automatically moved to " + shortClipPrep.getName());
                        
                        System.out.println("[SMART-ORCHESTRATOR] Success: Moved " + sib.getPlatform().getName() + " to stage 10");
                    }
                }
            }
        }
    }

    public List<CommentResponse> getComments(Long deliverableId) {
        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));
        
        return commentRepository.findByDeliverableOrderByCreatedAtDesc(deliverable).stream()
                .map(c -> new CommentResponse(
                        c.getId(),
                        c.getReviewer().getName(),
                        c.getText(),
                        c.getDecision(),
                        c.getCreatedAt()
                )).collect(Collectors.toList());
    }
}
