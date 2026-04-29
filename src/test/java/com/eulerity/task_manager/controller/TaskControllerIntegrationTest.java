package com.eulerity.task_manager.controller;

import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.model.Task;
import com.eulerity.task_manager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void createTask_shouldReturnCreated() throws Exception {
        String payload = """
                {
                  "title": "Prepare assessment",
                  "description": "Finish take-home project",
                  "dueDate": "2026-05-04",
                  "priority": "HIGH",
                  "status": "TODO"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Prepare assessment"))
                .andExpect(jsonPath("$.description").value("Finish take-home project"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-04"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void getAllTasks_shouldReturnOk() throws Exception {
        Task task = new Task(
                "Write tests",
                "Add integration tests",
                LocalDate.of(2026, 5, 5),
                Priority.MEDIUM,
                Status.IN_PROGRESS
        );
        taskRepository.save(task);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Write tests"))
                .andExpect(jsonPath("$[0].description").value("Add integration tests"))
                .andExpect(jsonPath("$[0].dueDate").value("2026-05-05"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    void getTaskById_shouldReturnOk() throws Exception {
        Task task = new Task(
                "Review code",
                "Review service layer",
                LocalDate.of(2026, 5, 6),
                Priority.LOW,
                Status.TODO
        );
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Review code"))
                .andExpect(jsonPath("$.description").value("Review service layer"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-06"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void updateTask_shouldReturnOk() throws Exception {
        Task task = new Task(
                "Old title",
                "Old description",
                LocalDate.of(2026, 5, 7),
                Priority.LOW,
                Status.TODO
        );
        Task savedTask = taskRepository.save(task);

        String payload = """
                {
                  "title": "New title",
                  "description": "New description",
                  "dueDate": "2026-05-09",
                  "priority": "HIGH",
                  "status": "DONE"
                }
                """;

        mockMvc.perform(put("/tasks/{id}", savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.description").value("New description"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-09"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void deleteTask_shouldReturnNoContent() throws Exception {
        Task task = new Task(
                "Cleanup",
                "Delete unused items",
                LocalDate.of(2026, 5, 8),
                Priority.MEDIUM,
                Status.IN_PROGRESS
        );
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isNoContent());
    }
}
