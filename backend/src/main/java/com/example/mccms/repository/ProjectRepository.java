package com.example.mccms.repository;

import com.example.mccms.model.Project;
import com.example.mccms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreator(User creator);
    long countByCreator(User creator);
}
