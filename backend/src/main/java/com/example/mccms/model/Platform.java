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

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "api_type")
    private String apiType;

    @Column(name = "aspect_ratio")
    private String aspectRatio;

    @Column(name = "max_duration_seconds")
    private Integer maxDurationSeconds;

    @Column(name = "caption_max_length")
    private Integer captionMaxLength;
}
