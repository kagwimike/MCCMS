package com.example.mccms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PLATFORMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "api_type", length = 20)
    private String apiType; // REAL, MOCKED

    @Column(name = "aspect_ratio", length = 10)
    private String aspectRatio;

    @Column(name = "max_duration_seconds")
    private Integer maxDurationSeconds;

    @Column(name = "caption_max_length")
    private Integer captionMaxLength;
}
