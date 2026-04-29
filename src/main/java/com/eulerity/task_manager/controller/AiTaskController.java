package com.eulerity.task_manager.controller;

import com.eulerity.task_manager.dto.AiSuggestRequest;
import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.service.AiTaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class AiTaskController {

    private final AiTaskService aiTaskService;

    public AiTaskController(AiTaskService aiTaskService) {
        this.aiTaskService = aiTaskService;
    }

    @PostMapping("/suggest")
    public ResponseEntity<AiSuggestResponse> suggestTask(@Valid @RequestBody AiSuggestRequest request) {
        AiSuggestResponse response = aiTaskService.suggestTask(request.getPrompt());
        return ResponseEntity.ok(response);
    }
}
