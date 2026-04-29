package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.dto.TaskBreakdownResponse;
import com.eulerity.task_manager.dto.TaskResponse;
import com.eulerity.task_manager.dto.TaskSummaryResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class GeminiAiClientTest {

    @Test
    void suggestTask_shouldUseFallbackWhenApiKeyIsMissing() {
        LocalFallbackAiClient fallbackAiClient = spy(new LocalFallbackAiClient());
        GeminiAiClient geminiAiClient = new GeminiAiClient(
                "",
                "gemini-2.5-flash-lite",
                fallbackAiClient,
                new ObjectMapper()
        );

        AiSuggestResponse response = geminiAiClient.suggestTask("remind me to submit assessment tomorrow");

        verify(fallbackAiClient).suggestTask(anyString());
        assertNotNull(response);
        assertNotNull(response.getTitle());
        assertNotNull(response.getDescription());
        assertNotNull(response.getPriority());
        assertEquals(Status.TODO, response.getStatus());
    }

    @Test
    void summarizeTask_shouldUseFallbackWhenApiKeyIsMissing() {
        LocalFallbackAiClient fallbackAiClient = spy(new LocalFallbackAiClient());
        GeminiAiClient geminiAiClient = new GeminiAiClient(
                "",
                "gemini-2.5-flash-lite",
                fallbackAiClient,
                new ObjectMapper()
        );

        TaskResponse task = new TaskResponse(
                11L,
                "Finish project",
                "Complete all pending work.",
                LocalDate.now().plusDays(2),
                Priority.HIGH,
                Status.IN_PROGRESS
        );

        TaskSummaryResponse response = geminiAiClient.summarizeTask(task);

        assertNotNull(response);
        assertEquals(11L, response.getTaskId());
        assertNotNull(response.getSummary());
        verify(fallbackAiClient).summarizeTask(task);
    }

    @Test
    void breakdownTask_shouldUseFallbackWhenApiKeyIsMissing() {
        LocalFallbackAiClient fallbackAiClient = spy(new LocalFallbackAiClient());
        GeminiAiClient geminiAiClient = new GeminiAiClient(
                "",
                "gemini-2.5-flash-lite",
                fallbackAiClient,
                new ObjectMapper()
        );

        TaskResponse task = new TaskResponse(
                12L,
                "Prepare submission",
                "Finalize and submit the project.",
                LocalDate.now().plusDays(1),
                Priority.MEDIUM,
                Status.TODO
        );

        TaskBreakdownResponse response = geminiAiClient.breakdownTask(task);

        assertNotNull(response);
        assertEquals(12L, response.getTaskId());
        assertNotNull(response.getSubtasks());
        assertFalse(response.getSubtasks().isEmpty());
        verify(fallbackAiClient).breakdownTask(task);
    }

    @Test
    void normalizePastDueDate_shouldSetDueDateToNullWhenDateIsInThePast() {
        LocalFallbackAiClient fallbackAiClient = spy(new LocalFallbackAiClient());
        GeminiAiClient geminiAiClient = new GeminiAiClient(
                "fake-key",
                "gemini-2.5-flash-lite",
                fallbackAiClient,
                new ObjectMapper()
        );

        AiSuggestResponse suggestion = new AiSuggestResponse(
                "Finish project",
                "Finish the backend task manager project.",
                LocalDate.now().minusDays(1),
                Priority.HIGH,
                Status.TODO
        );

        geminiAiClient.normalizePastDueDate(suggestion);

        assertNull(suggestion.getDueDate());
    }

    @Test
    void normalizePastDueDate_shouldSetDueDateToNullWhenDueDateIsYesterday() {
        LocalFallbackAiClient fallbackAiClient = spy(new LocalFallbackAiClient());
        GeminiAiClient geminiAiClient = new GeminiAiClient(
                "fake-key",
                "gemini-2.5-flash-lite",
                fallbackAiClient,
                new ObjectMapper()
        );

        AiSuggestResponse suggestion = new AiSuggestResponse(
                "Submit project",
                "Submit the internship project.",
                LocalDate.now().minusDays(1),
                Priority.HIGH,
                Status.TODO
        );

        geminiAiClient.normalizePastDueDate(suggestion);

        assertNull(suggestion.getDueDate());
    }

    @Test
    void normalizePastDueDate_shouldLeaveDueDateUnchangedWhenDueDateIsTomorrow() {
        LocalFallbackAiClient fallbackAiClient = spy(new LocalFallbackAiClient());
        GeminiAiClient geminiAiClient = new GeminiAiClient(
                "fake-key",
                "gemini-2.5-flash-lite",
                fallbackAiClient,
                new ObjectMapper()
        );

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        AiSuggestResponse suggestion = new AiSuggestResponse(
                "Submit project",
                "Submit the internship project.",
                tomorrow,
                Priority.HIGH,
                Status.TODO
        );

        geminiAiClient.normalizePastDueDate(suggestion);

        assertEquals(tomorrow, suggestion.getDueDate());
    }
}
