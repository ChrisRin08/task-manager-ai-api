package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.dto.TaskBreakdownResponse;
import com.eulerity.task_manager.dto.TaskResponse;
import com.eulerity.task_manager.dto.TaskSummaryResponse;

public interface AiClient {

    AiSuggestResponse suggestTask(String prompt);

    TaskSummaryResponse summarizeTask(TaskResponse task);

    TaskBreakdownResponse breakdownTask(TaskResponse task);
}
