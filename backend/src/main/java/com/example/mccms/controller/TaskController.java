package com.example.mccms.controller;

import com.example.mccms.dto.TaskRequest;
import com.example.mccms.dto.TaskResponse;
import com.example.mccms.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/deliverable/{id}")
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(id, request));
    }

    @GetMapping("/deliverable/{id}")
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTasksForDeliverable(id));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleTask(@PathVariable Long id) {
        taskService.toggleTask(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
