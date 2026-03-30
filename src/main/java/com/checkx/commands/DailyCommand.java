package com.checkx.commands;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.domain.Stats;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.ui.ConsoleColors;
import picocli.CommandLine.Command;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Shows today's habits with completion status.
 */
@Command(
    name = "daily",
    description = "Show today's habits"
)
public class DailyCommand implements Runnable {

    private final HabitRepository repository = new JsonHabitRepository();

    @Override
    public void run() {
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println(ConsoleColors.warning("No habits yet! Add your first habit with: checkx add [name]"));
            return;
        }

        // Header
        ConsoleColors.printSeparator();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        System.out.println(ConsoleColors.title("📅 " + today));
        ConsoleColors.printLine();

        // Display habits
        for (Habit habit : habits) {
            String habitLine = formatHabitLine(habit);
            System.out.println(habitLine);
        }

        // Footer with stats
        ConsoleColors.printLine();
        Stats stats = new Stats(habits);
        System.out.println(ConsoleColors.info("Progress: ") + 
                          ConsoleColors.bold(stats.getCompletedToday() + "/" + stats.getTotalHabits()) +
                          ConsoleColors.muted(" (" + (int)stats.getCompletionPercentage() + "%)"));
        System.out.println(ConsoleColors.muted(stats.getMotivationalMessage()));
        ConsoleColors.printSeparator();
    }

    private String formatHabitLine(Habit habit) {
        String checkbox = habit.isCompletedToday() 
            ? ConsoleColors.habitCompleted(habit.getIcon() + " " + habit.getName())
            : ConsoleColors.habitPending(habit.getIcon() + " " + habit.getName());
        
        String streakInfo = habit.getStreak() > 0 
            ? ConsoleColors.streak(habit.getStreak() + "d")
            : ConsoleColors.muted("0d");
        
        // Pad to align streaks
        int padding = 50 - habit.getName().length();
        return checkbox + " ".repeat(Math.max(1, padding)) + streakInfo;
    }
}
