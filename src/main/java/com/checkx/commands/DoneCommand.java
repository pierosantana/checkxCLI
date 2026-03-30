package com.checkx.commands;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.ui.ConsoleColors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Optional;

/**
 * Marks a habit as completed for today.
 */
@Command(
    name = "done",
    description = "Mark a habit as completed"
)
public class DoneCommand implements Runnable {

    @Parameters(
        index = "0..*",
        description = "Name of the habit to complete",
        arity = "1..*"
    )
    private String[] habitName;

    private final HabitRepository repository = new JsonHabitRepository();

    @Override
    public void run() {
        if (habitName == null || habitName.length == 0) {
            System.out.println(ConsoleColors.error("Usage: checkx done [habit name]"));
            return;
        }

        String name = String.join(" ", habitName);
        Optional<Habit> habitOpt = repository.findByName(name);

        if (habitOpt.isEmpty()) {
            System.out.println(ConsoleColors.error("Habit not found: " + name));
            System.out.println(ConsoleColors.muted("Tip: Use 'checkx list' to see all habits"));
            return;
        }

        Habit habit = habitOpt.get();

        if (habit.isCompletedToday()) {
            System.out.println(ConsoleColors.warning(
                habit.getIcon() + " " + habit.getName() + " is already completed today!"
            ));
            return;
        }

        habit.complete();
        repository.save(habit);

        System.out.println(ConsoleColors.success(
            "✓ " + habit.getIcon() + " " + habit.getName() + " completed!"
        ));
        System.out.println(ConsoleColors.streak(habit.getStreak() + " day streak!"));
    }
}
