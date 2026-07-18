package com.example.mccms.service;

import com.example.mccms.dto.DashboardStats;
import com.example.mccms.model.Role;
import com.example.mccms.model.User;
import com.example.mccms.repository.ProjectRepository;
import com.example.mccms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public DashboardStats getStats(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalProjects;
        if (user.getRole().getName().equals(Role.ADMIN) || user.getRole().getName().equals(Role.REVIEWER)) {
            totalProjects = projectRepository.count();
        } else {
            totalProjects = projectRepository.countByCreator(user);
        }

        // Placeholder logic for pending reviews and at-risk deliverables
        // These will be fully implemented once the DELIVERABLES table is added
        long pendingReviews = 0;
        long atRiskDeliverables = 0;

        return new DashboardStats(totalProjects, pendingReviews, atRiskDeliverables);
    }
}
