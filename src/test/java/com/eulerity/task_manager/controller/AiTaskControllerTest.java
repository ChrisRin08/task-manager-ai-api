package com.eulerity.task_manager.controller;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.service.AiTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiTaskController.class)
class AiTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiTaskService aiTaskService;

    @Test
    void suggestTask_shouldReturnStructuredResponse() throws Exception {
        String prompt = "remind me to submit my internship project before Friday";
        AiSuggestResponse suggestion = new AiSuggestResponse(
                "Submit internship project",
                "Complete and submit the internship project before Friday.",
                LocalDate.of(2026, 5, 1),
                Priority.HIGH,
                Status.TODO
        );
        when(aiTaskService.suggestTask(prompt)).thenReturn(suggestion);

        String payload = """
                {
                  "prompt": "remind me to submit my internship project before Friday"
                }
                """;

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Submit internship project"))
                .andExpect(jsonPath("$.description").value("Complete and submit the internship project before Friday."))
                .andExpect(jsonPath("$.dueDate").value("2026-05-01"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(aiTaskService).suggestTask(prompt);
    }
}
