package com.eulerity.task_manager.controller;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.dto.TaskBreakdownResponse;
import com.eulerity.task_manager.dto.TaskSummaryResponse;
import com.eulerity.task_manager.exception.TaskNotFoundException;
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
import java.util.List;

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

    @Test
    void summarizeTask_shouldReturnSummary() throws Exception {
        TaskSummaryResponse summary = new TaskSummaryResponse(
                1L,
                "This task is about finishing the internship project before Friday."
        );
        when(aiTaskService.summarizeTask(1L)).thenReturn(summary);

        mockMvc.perform(post("/tasks/{id}/summarize", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.summary").value("This task is about finishing the internship project before Friday."));

        verify(aiTaskService).summarizeTask(1L);
    }

    @Test
    void breakdownTask_shouldReturnSubtasks() throws Exception {
        TaskBreakdownResponse breakdown = new TaskBreakdownResponse(
                1L,
                List.of(
                        "Review project requirements",
                        "Finish backend implementation",
                        "Run all tests",
                        "Update README",
                        "Submit final project"
                )
        );
        when(aiTaskService.breakdownTask(1L)).thenReturn(breakdown);

        mockMvc.perform(post("/tasks/{id}/breakdown", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value(1))
                .andExpect(jsonPath("$.subtasks.length()").value(5))
                .andExpect(jsonPath("$.subtasks[0]").value("Review project requirements"))
                .andExpect(jsonPath("$.subtasks[4]").value("Submit final project"));

        verify(aiTaskService).breakdownTask(1L);
    }

    @Test
    void summarizeTask_shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        when(aiTaskService.summarizeTask(999L))
                .thenThrow(new TaskNotFoundException("Task not found with id: 999"));

        mockMvc.perform(post("/tasks/{id}/summarize", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"));
    }
}
