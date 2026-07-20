package com.example.mccms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    @NotBlank
    private String text;

    @NotBlank
    private String decision; // APPROVED, REJECTED, NEUTRAL
}
