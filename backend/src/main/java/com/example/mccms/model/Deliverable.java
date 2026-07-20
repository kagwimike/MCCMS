package com.example.mccms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DELIVERABLES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deliverable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "deliverable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "relatedDeliverable", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "deliverable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "stage_updated_at")
    private LocalDateTime stageUpdatedAt = LocalDateTime.now();

    @Column(nullable = false, length = 20)
    private String status = "PENDING";
}
