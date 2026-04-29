package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.dto.TaskBreakdownResponse;
import com.eulerity.task_manager.dto.TaskResponse;
import com.eulerity.task_manager.dto.TaskSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiTaskService {

    private static final Logger log = LoggerFactory.getLogger(AiTaskService.class);

    private final AiClient aiClient;
    private final TaskService taskService;

    public AiTaskService(AiClient aiClient, TaskService taskService, @Value("${GEMINI_API_KEY:}") String geminiApiKey) {
        this.aiClient = aiClient;
        this.taskService = taskService;
        log.info("GEMINI_API_KEY is {}.", geminiApiKey == null || geminiApiKey.isBlank() ? "missing" : "present");
        log.info("AiClient bean selected: {}", aiClient.getClass().getSimpleName());
    }

    public AiSuggestResponse suggestTask(String prompt) {
        return aiClient.suggestTask(prompt);
    }

    public TaskSummaryResponse summarizeTask(Long taskId) {
        TaskResponse task = taskService.getTaskById(taskId);
        return aiClient.summarizeTask(task);
    }

    public TaskBreakdownResponse breakdownTask(Long taskId) {
        TaskResponse task = taskService.getTaskById(taskId);
        return aiClient.breakdownTask(task);
    }
}
