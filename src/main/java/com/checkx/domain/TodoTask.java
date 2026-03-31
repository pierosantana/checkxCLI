package com.checkx.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a quick to-do task for a specific day.
 */
public class TodoTask {

    private static final int MAX_TEXT_LENGTH = 35;

    private final String id;
    private String text;
    private LocalDate date;
    private boolean completed;

    public TodoTask(String text) {
        this(text, LocalDate.now());
    }

    public TodoTask(String text, LocalDate date) {
        this.id = UUID.randomUUID().toString();
        this.text = truncate(text);
        this.date = date;
        this.completed = false;
    }

    // Constructor for deserialization
    public TodoTask(String id, String text, LocalDate date, boolean completed) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.completed = completed;
    }

    public void complete() {
        this.completed = true;
    }

    public void uncomplete() {
        this.completed = false;
    }

    private static String truncate(String text) {
        if (text.length() > MAX_TEXT_LENGTH) {
            return text.substring(0, MAX_TEXT_LENGTH);
        }
        return text;
    }

    public static int getMaxTextLength() {
        return MAX_TEXT_LENGTH;
    }

    // Getters
    public String getId() { return id; }
    public String getText() { return text; }
    public LocalDate getDate() { return date; }
    public boolean isCompleted() { return completed; }

    public void setText(String text) {
        this.text = truncate(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoTask task = (TodoTask) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return (completed ? "[x] " : "[ ] ") + text;
    }
}
