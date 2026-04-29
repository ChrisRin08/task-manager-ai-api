package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String SYSTEM_PROMPT = """
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
            String rawSuggestionJson = callGemini(cleanedPrompt);
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

    private String callGemini(String prompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", SYSTEM_PROMPT + "\n\nUser prompt: " + prompt)
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

    private AiSuggestResponse parseSuggestion(String rawContent) throws Exception {
        String cleaned = stripCodeFences(rawContent.trim());
        AiSuggestResponse suggestion = objectMapper.readValue(cleaned, AiSuggestResponse.class);
        suggestion.setStatus(Status.TODO);
        normalizePastDueDate(suggestion);
        return suggestion;
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
}
