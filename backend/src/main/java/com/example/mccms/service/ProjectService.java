package com.example.mccms.service;

import com.example.mccms.dto.ProjectRequest;
import com.example.mccms.dto.ProjectResponse;
import com.example.mccms.model.Deliverable;
import com.example.mccms.model.Project;
import com.example.mccms.model.Role;
import com.example.mccms.model.User;
import com.example.mccms.repository.DeliverableRepository;
import com.example.mccms.repository.ProjectRepository;
import com.example.mccms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private final DeliverableRepository deliverableRepository;

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setCreator(creator);

        Project saved = projectRepository.save(project);
        auditService.log(creator, "CREATE", "PROJECT", "SUCCESS");

        return mapToResponse(saved);
    }

    public List<ProjectResponse> getProjects(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Project> projects;
        if (user.getRole().getName().equals(Role.ADMIN) || user.getRole().getName().equals(Role.REVIEWER)) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByCreator(user);
        }

        return projects.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, String userEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        validateOwnership(project, user);

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        Project saved = projectRepository.save(project);
        
        auditService.log(user, "UPDATE", "PROJECT", "SUCCESS: " + id);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteProject(Long id, String userEmail) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepository.findByEmail(userEmail).orElseThrow();

        validateOwnership(project, user);

        projectRepository.delete(project);
        auditService.log(user, "DELETE", "PROJECT", "SUCCESS: " + id);
    }

    private void validateOwnership(Project project, User user) {
        if (!user.getRole().getName().equals(Role.ADMIN) && 
            !project.getCreator().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: You do not own this project");
        }
    }

    private ProjectResponse mapToResponse(Project project) {
        List<Deliverable> deliverables = deliverableRepository.findByProject(project);
        int progress = 0;
        if (!deliverables.isEmpty()) {
            double totalProgress = 0;
            for (Deliverable d : deliverables) {
                // Calculate percentage based on current stage out of 14 total stages
                totalProgress += (d.getStage().getSortOrder() / 14.0) * 100;
            }
            progress = (int) (totalProgress / deliverables.size());
        }

        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getCreator().getName(),
                project.getCreatedAt(),
                progress
        );
    }
}
