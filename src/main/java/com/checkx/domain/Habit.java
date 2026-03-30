package com.checkx.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a habit to be tracked.
 */
public class Habit {
    private final String id;
    private String name;
    private String icon;
    private String comment;
    private int streak;
    private boolean completedToday;
    private LocalDate lastCompletedDate;
    private final LocalDate createdDate;

    public Habit(String name, String icon) {
        this(name, icon, null);
    }

    public Habit(String name, String icon, String comment) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.icon = icon;
        this.comment = comment;
        this.streak = 0;
        this.completedToday = false;
        this.lastCompletedDate = null;
        this.createdDate = LocalDate.now();
    }

    // Constructor for deserialization
    public Habit(String id, String name, String icon, int streak, 
                 boolean completedToday, LocalDate lastCompletedDate, 
                 LocalDate createdDate) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.streak = streak;
        this.completedToday = completedToday;
        this.lastCompletedDate = lastCompletedDate;
        this.createdDate = createdDate;
    }

    /**
     * Marks the habit as completed for today.
     * Updates streak accordingly.
     */
    public void complete() {
        if (completedToday) {
            return; // Already completed
        }

        LocalDate today = LocalDate.now();
        
        if (lastCompletedDate != null) {
            // Check if we're continuing a streak
            if (lastCompletedDate.equals(today.minusDays(1))) {
                streak++; // Continue streak
            } else if (lastCompletedDate.isBefore(today.minusDays(1))) {
                streak = 1; // Reset streak
            }
        } else {
            streak = 1; // First completion
        }

        this.completedToday = true;
        this.lastCompletedDate = today;
    }

    /**
     * Reverts today's completion.
     */
    public void uncomplete() {
        if (!completedToday) {
            return;
        }

        if (streak <= 1) {
            streak = 0;
            lastCompletedDate = null;
        } else {
            streak--;
            lastCompletedDate = LocalDate.now().minusDays(1);
        }

        completedToday = false;
    }

    /**
     * Resets daily completion status.
     * Should be called when a new day starts.
     */
    public void resetDailyStatus() {
        LocalDate today = LocalDate.now();
        
        if (lastCompletedDate != null && lastCompletedDate.isBefore(today.minusDays(1))) {
            // Streak broken
            streak = 0;
        }
        
        completedToday = false;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public int getStreak() {
        return streak;
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public LocalDate getLastCompletedDate() {
        return lastCompletedDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Habit habit = (Habit) o;
        return Objects.equals(id, habit.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s %s - %dd streak", icon, name, streak);
    }
}
