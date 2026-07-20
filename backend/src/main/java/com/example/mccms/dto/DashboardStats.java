package com.example.mccms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalProjects;
    private long pendingReviews;
    private long atRiskDeliverables;
}
