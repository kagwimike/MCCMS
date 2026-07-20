package com.example.mccms.service;

import com.example.mccms.dto.DashboardStats;
import com.example.mccms.model.Deliverable;
import com.example.mccms.model.Project;
import com.example.mccms.model.Role;
import com.example.mccms.model.User;
import com.example.mccms.repository.DeliverableRepository;
import com.example.mccms.repository.ProjectRepository;
import com.example.mccms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final DeliverableRepository deliverableRepository;
    private final UserRepository userRepository;

    public DashboardStats getStats(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        
        long totalProjects;
        long pendingReviews;
        
        if (user.getRole().getName().equals(Role.ADMIN) || user.getRole().getName().equals(Role.REVIEWER)) {
            totalProjects = projectRepository.count();
            pendingReviews = deliverableRepository.findAll().stream()
                    .filter(d -> d.getStatus().equals("PENDING")).count();
        } else {
            List<Project> myProjects = projectRepository.findByCreator(user);
            totalProjects = myProjects.size();
            pendingReviews = deliverableRepository.findAll().stream()
                    .filter(d -> d.getProject().getCreator().getId().equals(user.getId()) && d.getStatus().equals("PENDING"))
                    .count();
        }

        // Simplification for Milestone 5 demo
        long atRisk = deliverableRepository.findAll().stream()
                .filter(d -> d.getScheduledAt() != null && d.getScheduledAt().isBefore(LocalDateTime.now().plusHours(24)))
                .count();

        return new DashboardStats(totalProjects, pendingReviews, atRisk);
    }
}
