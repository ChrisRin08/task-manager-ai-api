package com.eulerity.task_manager.service;

import com.eulerity.task_manager.dto.AiSuggestResponse;
import com.eulerity.task_manager.model.Priority;
import com.eulerity.task_manager.model.Status;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

@Service
public class LocalFallbackAiClient implements AiClient {

    @Override
    public AiSuggestResponse suggestTask(String prompt) {
        String cleanedPrompt = prompt == null ? "" : prompt.trim();

        String title = extractTitle(cleanedPrompt);
        LocalDate dueDate = extractDueDate(cleanedPrompt);
        Priority priority = extractPriority(cleanedPrompt);
        String description = buildDescription(cleanedPrompt, dueDate);

        if (title.isBlank()) {
            title = "New Task";
        }

        return new AiSuggestResponse(
                title,
                description,
                dueDate,
                priority,
                Status.TODO
        );
    }

    private String extractTitle(String prompt) {
        String normalized = prompt.toLowerCase(Locale.ROOT)
                .replace("remind me to", "")
                .replace("please", "")
                .trim();

        int stopIndex = normalized.length();
        String[] markers = {" before ", " by ", " today", " tomorrow", " monday", " tuesday", " wednesday", " thursday", " friday", " saturday", " sunday"};
        for (String marker : markers) {
            int index = normalized.indexOf(marker);
            if (index >= 0 && index < stopIndex) {
                stopIndex = index;
            }
        }

        String titleCandidate = normalized.substring(0, stopIndex)
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (titleCandidate.isBlank()) {
            return "New Task";
        }

        StringBuilder title = new StringBuilder();
        for (String word : titleCandidate.split(" ")) {
            if (!title.isEmpty()) {
                title.append(' ');
            }
            title.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return title.toString();
    }

    private LocalDate extractDueDate(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        LocalDate today = LocalDate.now();

        if (lower.contains("today")) {
            return today;
        }
        if (lower.contains("tomorrow")) {
            return today.plusDays(1);
        }
        if (lower.contains("monday")) {
            return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        }
        if (lower.contains("tuesday")) {
            return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY));
        }
        if (lower.contains("wednesday")) {
            return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
        }
        if (lower.contains("thursday")) {
            return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));
        }
        if (lower.contains("friday")) {
            return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        }
        if (lower.contains("saturday")) {
            return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        }
        if (lower.contains("sunday")) {
            return today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        return today.plusDays(3);
    }

    private Priority extractPriority(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT);

        if (lower.contains("urgent")
                || lower.contains("asap")
                || lower.contains("important")
                || lower.contains("immediately")
                || lower.contains("before")) {
            return Priority.HIGH;
        }

        if (lower.contains("later") || lower.contains("whenever") || lower.contains("someday")) {
            return Priority.LOW;
        }

        return Priority.MEDIUM;
    }

    private String buildDescription(String prompt, LocalDate dueDate) {
        if (prompt.isBlank()) {
            return "Task suggested from prompt.";
        }
        return "Task suggested from prompt: \"" + prompt + "\". Due by " + dueDate + ".";
    }
}
