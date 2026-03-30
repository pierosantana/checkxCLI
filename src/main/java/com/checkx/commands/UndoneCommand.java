package com.checkx.commands;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.ui.ConsoleColors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Reverts a habit's completion for today.
 */
@Command(
    name = "undone",
    description = "Revert a habit's completion for today"
)
public class UndoneCommand implements Runnable {

    @Parameters(
        index = "0..*",
        description = "Name of the habit to undo",
        arity = "1..*"
    )
    private String[] habitName;

    private final HabitRepository repository = new JsonHabitRepository();

    @Override
    public void run() {
        if (habitName == null || habitName.length == 0) {
            System.out.println(ConsoleColors.error("Usage: checkx undone [habit name]"));
            return;
        }

        String name = String.join(" ", habitName);
        var habitOpt = repository.findByName(name);

        if (habitOpt.isEmpty()) {
            System.out.println(ConsoleColors.error("Habit not found: " + name));
            return;
        }

        Habit habit = habitOpt.get();

        if (!habit.isCompletedToday()) {
            System.out.println(ConsoleColors.warning(habit.getIcon() + " " + habit.getName() + " is not completed today"));
            return;
        }

        int oldStreak = habit.getStreak();
        habit.uncomplete();
        repository.save(habit);

        System.out.println(ConsoleColors.success("↩ " + habit.getIcon() + " " + habit.getName() + " marked as not completed"));
        System.out.println(ConsoleColors.muted("Streak: " + oldStreak + "d → " + habit.getStreak() + "d"));
    }
}
