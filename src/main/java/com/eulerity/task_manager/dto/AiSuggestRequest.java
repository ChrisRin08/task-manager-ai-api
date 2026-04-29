package com.eulerity.task_manager.dto;

import jakarta.validation.constraints.NotBlank;

public class AiSuggestRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;

    public AiSuggestRequest() {
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
