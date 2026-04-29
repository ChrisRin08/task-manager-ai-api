package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.dto.TaskBreakdownResponse;
import com.eulerity.task_manager.dto.TaskResponse;
import com.eulerity.task_manager.dto.TaskSummaryResponse;
import com.eulerity.task_manager.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Primary
@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${GEMINI_API_KEY:}')")
public class GeminiAiClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiClient.class);

    private static final String SUGGEST_SYSTEM_PROMPT = """
            You convert user task prompts into JSON for a task manager.
            Return only valid JSON with this exact shape:
            {
              "title": "string",
              "description": "string",
              "dueDate": "YYYY-MM-DD or null",
              "priority": "LOW, MEDIUM, or HIGH",
              "status": "TODO"
            }
            Do not include markdown or extra text.
            """;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            You summarize one task for a task manager.
            Return only valid JSON with this exact shape:
            {
              "summary": "string"
            }
            Do not include markdown or extra text.
            """;

    private static final String BREAKDOWN_SYSTEM_PROMPT = """
            You break one task into actionable subtasks.
            Return only valid JSON with this exact shape:
            {
              "subtasks": ["string", "string", "string"]
            }
            Include 3-7 concise subtasks.
            Do not include markdown or extra text.
            """;

    private final String apiKey;
    private final String model;
    private final LocalFallbackAiClient fallbackAiClient;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiAiClient(
            @Value("${GEMINI_API_KEY:}") String apiKey,
            @Value("${GEMINI_MODEL:gemini-2.5-flash-lite}") String model,
            LocalFallbackAiClient fallbackAiClient,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.fallbackAiClient = fallbackAiClient;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        log.info("GeminiAiClient initialized. GEMINI_API_KEY is present. Model={}", model);
    }

    @Override
    public AiSuggestResponse suggestTask(String prompt) {
        String cleanedPrompt = prompt == null ? "" : prompt.trim();

        if (cleanedPrompt.isBlank() || apiKey.isBlank()) {
            log.info("Using LocalFallbackAiClient because prompt is blank or GEMINI_API_KEY is missing.");
            return fallbackAiClient.suggestTask(cleanedPrompt);
        }

        try {
            String rawSuggestionJson = callGemini(cleanedPrompt, SUGGEST_SYSTEM_PROMPT);
            AiSuggestResponse suggestion = parseSuggestion(rawSuggestionJson);
            if (isValidSuggestion(suggestion)) {
                log.info("Gemini suggestion succeeded. Model={}", model);
                return suggestion;
            }
            log.warn("Gemini returned invalid suggestion shape. Falling back to LocalFallbackAiClient.");
        } catch (RestClientResponseException ex) {
            log.warn(
                    "Gemini HTTP failure. status={}, errorType={}, message={}. Falling back to LocalFallbackAiClient.",
                    ex.getStatusCode(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
        } catch (Exception ex) {
            log.warn(
                    "Gemini call failed. errorType={}, message={}. Falling back to LocalFallbackAiClient.",
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
        }

        return fallbackAiClient.suggestTask(cleanedPrompt);
    }

    @Override
    public TaskSummaryResponse summarizeTask(TaskResponse task) {
        if (apiKey.isBlank()) {
            log.info("Using LocalFallbackAiClient for summarizeTask because GEMINI_API_KEY is missing.");
            return fallbackAiClient.summarizeTask(task);
        }

        try {
            String rawSummaryJson = callGemini(buildTaskContext(task), SUMMARY_SYSTEM_PROMPT);
            TaskSummaryResponse summary = parseSummary(rawSummaryJson, task.getId());
            if (isValidSummary(summary)) {
                log.info("Gemini summarize succeeded. Model={}", model);
                return summary;
            }
            log.warn("Gemini returned invalid summary shape. Falling back to LocalFallbackAiClient.");
        } catch (RestClientResponseException ex) {
            log.warn(
                    "Gemini HTTP failure for summarizeTask. status={}, errorType={}, message={}. Falling back to LocalFallbackAiClient.",
                    ex.getStatusCode(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
        } catch (Exception ex) {
            log.warn(
                    "Gemini summarizeTask failed. errorType={}, message={}. Falling back to LocalFallbackAiClient.",
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
        }

        return fallbackAiClient.summarizeTask(task);
    }

    @Override
    public TaskBreakdownResponse breakdownTask(TaskResponse task) {
        if (apiKey.isBlank()) {
            log.info("Using LocalFallbackAiClient for breakdownTask because GEMINI_API_KEY is missing.");
            return fallbackAiClient.breakdownTask(task);
        }

        try {
            String rawBreakdownJson = callGemini(buildTaskContext(task), BREAKDOWN_SYSTEM_PROMPT);
            TaskBreakdownResponse breakdown = parseBreakdown(rawBreakdownJson, task.getId());
            if (isValidBreakdown(breakdown)) {
                log.info("Gemini breakdown succeeded. Model={}", model);
                return breakdown;
            }
            log.warn("Gemini returned invalid breakdown shape. Falling back to LocalFallbackAiClient.");
        } catch (RestClientResponseException ex) {
            log.warn(
                    "Gemini HTTP failure for breakdownTask. status={}, errorType={}, message={}. Falling back to LocalFallbackAiClient.",
                    ex.getStatusCode(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
        } catch (Exception ex) {
            log.warn(
                    "Gemini breakdownTask failed. errorType={}, message={}. Falling back to LocalFallbackAiClient.",
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
        }

        return fallbackAiClient.breakdownTask(task);
    }

    private String callGemini(String prompt, String systemPrompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", systemPrompt + "\n\nUser input:\n" + prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json"
                )
        );

        String apiResponse = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/{model}:generateContent")
                        .build(model))
                .header("x-goog-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        if (apiResponse == null || apiResponse.isBlank()) {
            throw new IllegalStateException("Gemini response body is empty");
        }

        JsonNode root = objectMapper.readTree(apiResponse);
        JsonNode contentNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
            throw new IllegalStateException("Gemini response content is empty");
        }
        return contentNode.asText();
    }

    private String buildTaskContext(TaskResponse task) {
        return "Task ID: " + task.getId() + "\n"
                + "Title: " + safeText(task.getTitle()) + "\n"
                + "Description: " + safeText(task.getDescription()) + "\n"
                + "Due Date: " + (task.getDueDate() == null ? "null" : task.getDueDate()) + "\n"
                + "Priority: " + task.getPriority() + "\n"
                + "Status: " + task.getStatus();
    }

    private AiSuggestResponse parseSuggestion(String rawContent) throws Exception {
        String cleaned = stripCodeFences(rawContent.trim());
        AiSuggestResponse suggestion = objectMapper.readValue(cleaned, AiSuggestResponse.class);
        suggestion.setStatus(Status.TODO);
        normalizePastDueDate(suggestion);
        return suggestion;
    }

    private TaskSummaryResponse parseSummary(String rawContent, Long taskId) throws Exception {
        String cleaned = stripCodeFences(rawContent.trim());
        TaskSummaryResponse summary = objectMapper.readValue(cleaned, TaskSummaryResponse.class);
        summary.setTaskId(taskId);
        return summary;
    }

    private TaskBreakdownResponse parseBreakdown(String rawContent, Long taskId) throws Exception {
        String cleaned = stripCodeFences(rawContent.trim());
        TaskBreakdownResponse breakdown = objectMapper.readValue(cleaned, TaskBreakdownResponse.class);
        breakdown.setTaskId(taskId);
        return breakdown;
    }

    void normalizePastDueDate(AiSuggestResponse suggestion) {
        if (suggestion == null || suggestion.getDueDate() == null) {
            return;
        }
        if (suggestion.getDueDate().isBefore(LocalDate.now())) {
            log.warn("AI returned a past dueDate. Normalizing dueDate to null.");
            suggestion.setDueDate(null);
        }
    }

    private String stripCodeFences(String text) {
        String cleaned = text;
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```[a-zA-Z]*\\s*", "");
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }
        return cleaned.trim();
    }

    private boolean isValidSuggestion(AiSuggestResponse suggestion) {
        return suggestion != null
                && suggestion.getTitle() != null
                && !suggestion.getTitle().isBlank()
                && suggestion.getDescription() != null
                && !suggestion.getDescription().isBlank()
                && suggestion.getPriority() != null
                && suggestion.getStatus() != null;
    }

    private boolean isValidSummary(TaskSummaryResponse summary) {
        return summary != null && summary.getSummary() != null && !summary.getSummary().isBlank();
    }

    private boolean isValidBreakdown(TaskBreakdownResponse breakdown) {
        return breakdown != null
                && breakdown.getSubtasks() != null
                && !breakdown.getSubtasks().isEmpty()
                && breakdown.getSubtasks().stream().allMatch(item -> item != null && !item.isBlank());
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "(empty)";
        }
        return value;
    }
}
