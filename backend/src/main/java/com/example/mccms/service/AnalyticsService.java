package com.example.mccms.service;

import com.example.mccms.dto.AnalyticsSummary;
import com.example.mccms.model.Deliverable;
import com.example.mccms.model.Role;
import com.example.mccms.model.User;
import com.example.mccms.repository.DeliverableRepository;
import com.example.mccms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final DeliverableRepository deliverableRepository;
    private final UserRepository userRepository;

    public AnalyticsSummary getSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        
        List<Deliverable> allRelevant;
        if (user.getRole().getName().equals(Role.ADMIN)) {
            allRelevant = deliverableRepository.findAll();
        } else {
            allRelevant = deliverableRepository.findAll().stream()
                    .filter(d -> d.getProject().getCreator().getId().equals(user.getId()))
                    .collect(Collectors.toList());
        }

        // 1. Platform Distribution (All deliverables)
        Map<String, Long> platformDist = allRelevant.stream()
                .collect(Collectors.groupingBy(d -> d.getPlatform().getName(), Collectors.counting()));

        // 2. Status Distribution (Health)
        Map<String, Long> statusDist = allRelevant.stream()
                .collect(Collectors.groupingBy(Deliverable::getStatus, Collectors.counting()));

        // 3. Upload Consistency (Published only)
        Map<String, Long> consistency = new HashMap<>();
        List<Deliverable> published = allRelevant.stream()
                .filter(d -> d.getStatus().equals("PUBLISHED"))
                .toList();

        published.forEach(d -> {
            if (d.getPublishedAt() != null) {
                String day = d.getPublishedAt().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                consistency.put(day, consistency.getOrDefault(day, 0L) + 1);
            }
        });

        // 4. Creator Performance (Top Performers)
        Map<User, List<Deliverable>> byCreator = allRelevant.stream()
                .collect(Collectors.groupingBy(d -> d.getProject().getCreator()));
        
        List<AnalyticsSummary.CreatorPerformance> performers = byCreator.entrySet().stream()
                .map(entry -> {
                    long total = entry.getValue().size();
                    long approved = entry.getValue().stream().filter(d -> d.getStatus().equals("APPROVED") || d.getStatus().equals("PUBLISHED")).count();
                    double eff = total > 0 ? (approved * 100.0 / total) : 0;
                    return new AnalyticsSummary.CreatorPerformance(entry.getKey().getName(), total, Math.round(eff * 10.0) / 10.0);
                })
                .sorted(Comparator.comparing(AnalyticsSummary.CreatorPerformance::getVolume).reversed())
                .limit(5)
                .toList();

        // 5. Efficiency Calculation
        long totalTasks = allRelevant.size();
        long completedTasks = allRelevant.stream().filter(d -> d.getStatus().equals("PUBLISHED")).count();
        double overallEff = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;

        return new AnalyticsSummary(
                platformDist,
                consistency,
                statusDist,
                performers,
                published.size(),
                allRelevant.size(),
                Math.round(overallEff * 10.0) / 10.0
        );
    }
}
