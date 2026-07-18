package com.example.mccms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DeliverableResponse {
    private Long id;
    private String platformName;
    private String stageName;
    private Integer stageOrder;
    private String caption;
    private String mediaUrl;
    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime stageUpdatedAt;
}
