package com.checkx.domain;

import java.util.List;

/**
 * Calculates statistics from a list of habits.
 */
public class Stats {
    private final List<Habit> habits;

    public Stats(List<Habit> habits) {
        this.habits = habits;
    }

    public int getTotalHabits() {
        return habits.size();
    }

    public int getCompletedToday() {
        return (int) habits.stream()
                .filter(Habit::isCompletedToday)
                .count();
    }

    public int getPendingToday() {
        return getTotalHabits() - getCompletedToday();
    }

    public double getCompletionPercentage() {
        if (getTotalHabits() == 0) return 0.0;
        return (double) getCompletedToday() / getTotalHabits() * 100;
    }

    public int getAverageStreak() {
        if (habits.isEmpty()) return 0;
        return (int) habits.stream()
                .mapToInt(Habit::getStreak)
                .average()
                .orElse(0.0);
    }

    public int getBestStreak() {
        return habits.stream()
                .mapToInt(Habit::getStreak)
                .max()
                .orElse(0);
    }

    public Habit getTopHabit() {
        return habits.stream()
                .max((h1, h2) -> Integer.compare(h1.getStreak(), h2.getStreak()))
                .orElse(null);
    }

    public String getMotivationalMessage() {
        double percentage = getCompletionPercentage();
        
        if (percentage >= 100) {
            return "🌟 Perfect day! You're unstoppable!";
        } else if (percentage >= 75) {
            return "✨ Crushing it! Keep the momentum!";
        } else if (percentage >= 50) {
            return "💪 Good progress! Don't stop now!";
        } else if (percentage >= 25) {
            return "🎯 You got this! A few more to go!";
        } else {
            return "🚀 Let's start strong! Every step counts!";
        }
    }
}
