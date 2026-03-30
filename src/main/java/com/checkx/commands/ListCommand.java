package com.checkx.commands;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.ui.ConsoleColors;
import picocli.CommandLine.Command;

import java.util.List;

/**
 * Lists all habits.
 */
@Command(
    name = "list",
    description = "List all habits"
)
public class ListCommand implements Runnable {

    private final HabitRepository repository = new JsonHabitRepository();

    @Override
    public void run() {
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println(ConsoleColors.warning("No habits yet! Add your first habit with: checkx add [name]"));
            return;
        }

        System.out.println(ConsoleColors.title("📋 All Habits"));
        System.out.println();

        for (int i = 0; i < habits.size(); i++) {
            Habit habit = habits.get(i);
            String number = ConsoleColors.muted(String.format("%2d. ", i + 1));
            String icon = habit.getIcon();
            String name = ConsoleColors.bold(habit.getName());
            String streak = habit.getStreak() > 0 
                ? ConsoleColors.streak(habit.getStreak() + "d")
                : ConsoleColors.muted("0d");
            
            System.out.println(number + icon + " " + name + " - " + streak);
        }

        System.out.println();
        System.out.println(ConsoleColors.muted("Total: " + habits.size() + " habits"));
    }
}
