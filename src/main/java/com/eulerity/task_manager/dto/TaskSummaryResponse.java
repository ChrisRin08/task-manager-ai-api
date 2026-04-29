package com.eulerity.task_manager.dto;

public class TaskSummaryResponse {

    private Long taskId;
    private String summary;

    public TaskSummaryResponse() {
    }

    public TaskSummaryResponse(Long taskId, String summary) {
        this.taskId = taskId;
        this.summary = summary;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
