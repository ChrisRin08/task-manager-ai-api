package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;

public interface AiClient {

    AiSuggestResponse suggestTask(String prompt);
}
