package com.example.mccms.repository;

import com.example.mccms.model.Deliverable;
import com.example.mccms.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByDeliverableOrderByCreatedAtAsc(Deliverable deliverable);
}
