package com.example.mccms.service;

import com.example.mccms.model.Deliverable;
import com.example.mccms.model.Stage;
import com.example.mccms.model.User;
import com.example.mccms.repository.ConnectedAccountRepository;
import com.example.mccms.repository.DeliverableRepository;
import com.example.mccms.repository.StageRepository;
import com.example.mccms.repository.UserRepository;
import com.example.mccms.service.publish.PlatformPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublishingService {

    private final List<PlatformPublisher> publishers;
    private final DeliverableRepository deliverableRepository;
    private final StageRepository stageRepository;
    private final UserRepository userRepository;
    private final ConnectedAccountRepository connectedAccountRepository; // 🔑 Add vault
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional
    public String publishDeliverable(Long deliverableId, String userEmail) throws Exception {
        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));
        
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        // Guardrail: Idempotency Check
        if (deliverable.getStatus().equals("PUBLISHED")) {
            throw new RuntimeException("This content has already been published.");
        }

        // Guardrail: Explicit approval required
        if (!deliverable.getStatus().equals("APPROVED")) {
            throw new RuntimeException("Deliverable must be APPROVED before publishing.");
        }

        PlatformPublisher publisher = publishers.stream()
                .filter(p -> p.getPlatformName().equalsIgnoreCase(deliverable.getPlatform().getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No publisher found for " + deliverable.getPlatform().getName()));

        // 1. Call Third-Party API
        String resultUrl = publisher.publish(deliverable);

        // 2. Update Status to Published
        Stage publishedStage = stageRepository.findAll().stream()
                .filter(s -> s.getName().equalsIgnoreCase("Published"))
                .findFirst()
                .orElseThrow();

        deliverable.setStage(publishedStage);
        deliverable.setStatus("PUBLISHED");
        deliverable.setPublishedAt(LocalDateTime.now());
        deliverable.setMediaUrl(resultUrl); // Store the live URL
        deliverableRepository.save(deliverable);

        // 3. Log and Notify
        auditService.log(user, "PUBLISH", "DELIVERABLE", "SUCCESS: " + deliverable.getPlatform().getName());
        notificationService.createNotification(deliverable.getProject().getCreator(), 
                deliverable, "SYSTEM", "SUCCESS: Published to " + deliverable.getPlatform().getName());

        return resultUrl;
    }
}
