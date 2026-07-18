package com.example.mccms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStats {
    private long totalProjects;
    private long pendingReviews;
    private long atRiskDeliverables;
}
