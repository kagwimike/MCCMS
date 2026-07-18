package com.example.mccms.controller;

import com.example.mccms.model.Stage;
import com.example.mccms.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StageController {

    private final StageRepository stageRepository;

    @GetMapping
    public ResponseEntity<List<Stage>> getStages() {
        return ResponseEntity.ok(stageRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder")));
    }
}
