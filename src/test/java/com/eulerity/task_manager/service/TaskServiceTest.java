package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.TaskRequest;
import com.eulerity.task_manager.dto.TaskResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import com.eulerity.task_manager.model.Task;
import com.eulerity.task_manager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_shouldSaveTaskAndReturnResponse() {
        TaskRequest request = buildTaskRequest(
                "Submit assignment",
                "Submit backend assignment",
                LocalDate.of(2026, 5, 1),
                Priority.HIGH,
                Status.TODO
        );

        Task savedTask = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getDueDate(),
                request.getPriority(),
                request.getStatus()
        );
        setTaskId(savedTask, 1L);

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(request);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());
        Task taskPassedToRepository = taskCaptor.getValue();

        assertEquals(request.getTitle(), taskPassedToRepository.getTitle());
        assertEquals(request.getDescription(), taskPassedToRepository.getDescription());
        assertEquals(request.getDueDate(), taskPassedToRepository.getDueDate());
        assertEquals(request.getPriority(), taskPassedToRepository.getPriority());
        assertEquals(request.getStatus(), taskPassedToRepository.getStatus());

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals(request.getDescription(), response.getDescription());
        assertEquals(request.getDueDate(), response.getDueDate());
        assertEquals(request.getPriority(), response.getPriority());
        assertEquals(request.getStatus(), response.getStatus());
    }

    @Test
    void getAllTasks_shouldReturnMappedResponses() {
        Task taskOne = new Task("Task One", "Description One", LocalDate.of(2026, 5, 2), Priority.LOW, Status.TODO);
        Task taskTwo = new Task("Task Two", "Description Two", LocalDate.of(2026, 5, 3), Priority.MEDIUM, Status.IN_PROGRESS);
        setTaskId(taskOne, 1L);
        setTaskId(taskTwo, 2L);

        when(taskRepository.findAll()).thenReturn(List.of(taskOne, taskTwo));

        List<TaskResponse> responses = taskService.getAllTasks();

        verify(taskRepository, times(1)).findAll();
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("Task One", responses.get(0).getTitle());
        assertEquals(2L, responses.get(1).getId());
        assertEquals("Task Two", responses.get(1).getTitle());
    }

    @Test
    void getTaskById_shouldReturnTaskWhenFound() {
        Task task = new Task("Read docs", "Read API docs", LocalDate.of(2026, 5, 4), Priority.MEDIUM, Status.TODO);
        setTaskId(task, 5L);

        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(5L);

        verify(taskRepository, times(1)).findById(5L);
        assertEquals(5L, response.getId());
        assertEquals("Read docs", response.getTitle());
        assertEquals("Read API docs", response.getDescription());
        assertEquals(LocalDate.of(2026, 5, 4), response.getDueDate());
        assertEquals(Priority.MEDIUM, response.getPriority());
        assertEquals(Status.TODO, response.getStatus());
    }

    @Test
    void updateTask_shouldUpdateAndReturnResponse() {
        Task existingTask = new Task(
                "Old title",
                "Old description",
                LocalDate.of(2026, 5, 6),
                Priority.LOW,
                Status.TODO
        );
        setTaskId(existingTask, 7L);

        TaskRequest updateRequest = buildTaskRequest(
                "Updated title",
                "Updated description",
                LocalDate.of(2026, 5, 9),
                Priority.HIGH,
                Status.IN_PROGRESS
        );

        when(taskRepository.findById(7L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        TaskResponse response = taskService.updateTask(7L, updateRequest);

        verify(taskRepository, times(1)).findById(7L);
        verify(taskRepository, times(1)).save(existingTask);

        assertEquals(7L, response.getId());
        assertEquals("Updated title", response.getTitle());
        assertEquals("Updated description", response.getDescription());
        assertEquals(LocalDate.of(2026, 5, 9), response.getDueDate());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(Status.IN_PROGRESS, response.getStatus());
    }

    @Test
    void deleteTask_shouldDeleteWhenFound() {
        Task existingTask = new Task(
                "Cleanup",
                "Delete old tasks",
                LocalDate.of(2026, 5, 10),
                Priority.MEDIUM,
                Status.TODO
        );
        setTaskId(existingTask, 10L);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(existingTask));

        taskService.deleteTask(10L);

        verify(taskRepository, times(1)).findById(10L);
        verify(taskRepository, times(1)).delete(existingTask);
    }

    private TaskRequest buildTaskRequest(String title, String description, LocalDate dueDate, Priority priority, Status status) {
        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setDueDate(dueDate);
        request.setPriority(priority);
        request.setStatus(status);
        return request;
    }

    private void setTaskId(Task task, Long id) {
        try {
            Field idField = Task.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(task, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set task id in test", e);
        }
    }
}
