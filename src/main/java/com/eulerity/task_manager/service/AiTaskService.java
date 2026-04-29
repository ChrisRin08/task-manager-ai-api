package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import org.springframework.stereotype.Service;

@Service
public class AiTaskService {

    private final AiClient aiClient;

    public AiTaskService(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    public AiSuggestResponse suggestTask(String prompt) {
        return aiClient.suggestTask(prompt);
    }
}
