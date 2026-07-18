package com.example.mccms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String reviewerName;
    private String text;
    private String decision;
    private LocalDateTime createdAt;
}
