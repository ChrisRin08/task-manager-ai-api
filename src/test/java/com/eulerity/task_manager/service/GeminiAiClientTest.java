package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
