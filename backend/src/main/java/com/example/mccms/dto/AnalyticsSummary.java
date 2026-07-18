package com.example.mccms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummary {
    private Map<String, Long> platformDistribution;
    private Map<String, Long> consistencyData; // Day of Week -> Count
    private Map<String, Long> statusDistribution; // APPROVED, PENDING, etc
    private List<CreatorPerformance> topPerformers;
    private long totalPublished;
    private long totalActiveDeliverables;
    private double overallEfficiency;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatorPerformance {
        private String name;
        private long volume;
        private double efficiency; // Percentage of tasks approved
    }
}
