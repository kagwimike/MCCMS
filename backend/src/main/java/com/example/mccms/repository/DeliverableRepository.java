package com.example.mccms.repository;

import com.example.mccms.model.Deliverable;
import com.example.mccms.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliverableRepository extends JpaRepository<Deliverable, Long> {
    List<Deliverable> findByProject(Project project);
    
    /**
     * Efficiently finds deliverables that are due within a window and not yet published.
     */
    @Query("SELECT d FROM Deliverable d WHERE d.scheduledAt <= :window AND d.scheduledAt > :now AND d.stage.name <> 'Published'")
    List<Deliverable> findAtRiskDeliverables(@Param("now") LocalDateTime now, @Param("window") LocalDateTime window);
}
