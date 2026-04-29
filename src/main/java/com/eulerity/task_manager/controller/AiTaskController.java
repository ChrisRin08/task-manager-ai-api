package com.eulerity.task_manager.controller;

import com.eulerity.task_manager.dto.AiSuggestRequest;
import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.dto.TaskBreakdownResponse;
import com.eulerity.task_manager.dto.TaskSummaryResponse;
import com.eulerity.task_manager.service.AiTaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/{id}/summarize")
    public ResponseEntity<TaskSummaryResponse> summarizeTask(@PathVariable Long id) {
        TaskSummaryResponse response = aiTaskService.summarizeTask(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/breakdown")
    public ResponseEntity<TaskBreakdownResponse> breakdownTask(@PathVariable Long id) {
        TaskBreakdownResponse response = aiTaskService.breakdownTask(id);
        return ResponseEntity.ok(response);
    }
}
