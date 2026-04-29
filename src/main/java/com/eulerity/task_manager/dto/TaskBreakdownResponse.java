package com.eulerity.task_manager.dto;

import java.util.List;

public class TaskBreakdownResponse {

    private Long taskId;
    private List<String> subtasks;

    public TaskBreakdownResponse() {
    }

    public TaskBreakdownResponse(Long taskId, List<String> subtasks) {
        this.taskId = taskId;
        this.subtasks = subtasks;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public List<String> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<String> subtasks) {
        this.subtasks = subtasks;
    }
}
