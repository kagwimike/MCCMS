package com.example.mccms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "TASKS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliverable_id", nullable = false)
    private Deliverable deliverable;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "is_complete")
    private boolean isComplete = false;

    @Column(length = 20)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
