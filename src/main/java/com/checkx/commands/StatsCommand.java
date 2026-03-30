package com.checkx.commands;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.domain.Stats;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.ui.ConsoleColors;
import picocli.CommandLine.Command;

import java.util.List;

/**
 * Displays statistics about habits.
 */
@Command(
    name = "stats",
    description = "Show habit statistics"
)
public class StatsCommand implements Runnable {

    private final HabitRepository repository = new JsonHabitRepository();

    @Override
    public void run() {
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println(ConsoleColors.warning("No habits yet! Add your first habit with: checkx add [name]"));
            return;
        }

        Stats stats = new Stats(habits);

        // Header
        ConsoleColors.printSeparator();
        System.out.println(ConsoleColors.title("📊 CheckX Statistics"));
        ConsoleColors.printLine();

        // Overall stats
        System.out.println(ConsoleColors.info("Total habits:     ") + 
                          ConsoleColors.bold(String.valueOf(stats.getTotalHabits())));
        System.out.println(ConsoleColors.info("Avg streak:       ") + 
                          ConsoleColors.bold(stats.getAverageStreak() + "d"));
        System.out.println(ConsoleColors.info("Best streak:      ") + 
                          ConsoleColors.streak(stats.getBestStreak() + "d 🏆"));
        System.out.println(ConsoleColors.info("Today:            ") + 
                          ConsoleColors.bold(stats.getCompletedToday() + "/" + stats.getTotalHabits()) +
                          ConsoleColors.muted(" (" + (int)stats.getCompletionPercentage() + "%)"));

        // Progress bar
        System.out.println();
        ConsoleColors.progressBar(stats.getCompletedToday(), stats.getTotalHabits(), "Daily progress");

        // Top habit
        Habit topHabit = stats.getTopHabit();
        if (topHabit != null && topHabit.getStreak() > 0) {
            System.out.println();
            System.out.println(ConsoleColors.info("Top habit:        ") + 
                              ConsoleColors.bold(topHabit.getIcon() + " " + topHabit.getName()));
        }

        ConsoleColors.printSeparator();
    }
}
