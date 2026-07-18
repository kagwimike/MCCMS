package com.example.mccms.service;

import com.example.mccms.dto.TaskRequest;
import com.example.mccms.dto.TaskResponse;
import com.example.mccms.model.Deliverable;
import com.example.mccms.model.Task;
import com.example.mccms.repository.DeliverableRepository;
import com.example.mccms.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final DeliverableRepository deliverableRepository;

    @Transactional
    public TaskResponse createTask(Long deliverableId, TaskRequest request) {
        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));

        Task task = new Task();
        task.setDeliverable(deliverable);
        task.setTitle(request.getTitle());
        task.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    public List<TaskResponse> getTasksForDeliverable(Long deliverableId) {
        Deliverable deliverable = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new RuntimeException("Deliverable not found"));
        
        return taskRepository.findByDeliverableOrderByCreatedAtAsc(deliverable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setComplete(!task.isComplete());
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.isComplete(),
                task.getPriority()
        );
    }
}
