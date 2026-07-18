package com.example.mccms.service;

import com.example.mccms.model.Deliverable;
import com.example.mccms.repository.DeliverableRepository;
import com.example.mccms.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeadlineCheckTask {

    private final DeliverableRepository deliverableRepository;
    private final NotificationService notificationService;
    private final SystemSettingRepository systemSettingRepository;

    // Run every 15 minutes
    @Scheduled(fixedRate = 900000)
    public void checkDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        
        int warningHours = systemSettingRepository.findByKey("deadline_warning_hours")
                .map(s -> Integer.parseInt(s.getValue()))
                .orElse(24);

        LocalDateTime warningWindow = now.plusHours(warningHours);

        // Scalability refactor: Query only what we need instead of loading everything
        List<Deliverable> atRisk = deliverableRepository.findAtRiskDeliverables(now, warningWindow);
        
        for (Deliverable d : atRisk) {
            String message = String.format("Action Required: %s post for '%s' is due soon (%sh window) but still in %s stage!", 
                                           d.getPlatform().getName(), 
                                           d.getProject().getTitle(), 
                                           warningHours,
                                           d.getStage().getName());
            
            notificationService.createNotification(d.getProject().getCreator(), d, "DEADLINE_RISK", message);
        }
    }
}
