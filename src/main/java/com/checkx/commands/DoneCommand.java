package com.checkx.commands;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.ui.ConsoleColors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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
        Optional<Habit> habitOpt = resolveHabit(name);
        if (habitOpt.isEmpty()) return;

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

    private Optional<Habit> resolveHabit(String query) {
        var exact = repository.findByName(query);
        if (exact.isPresent()) return exact;

        List<Habit> matches = repository.searchByName(query);
        if (matches.isEmpty()) {
            System.out.println(ConsoleColors.error("Habit not found: " + query));
            System.out.println(ConsoleColors.muted("Tip: Use 'checkx list' to see all habits"));
            return Optional.empty();
        }
        if (matches.size() == 1) return Optional.of(matches.get(0));

        System.out.println(ConsoleColors.warning("Multiple habits match '" + query + "':"));
        for (int i = 0; i < matches.size(); i++) {
            Habit h = matches.get(i);
            System.out.println("  " + (i + 1) + ") " + h.getIcon() + " " + h.getName());
        }
        System.out.print("Choose (1-" + matches.size() + "): ");
        Scanner sc = new Scanner(System.in);
        try {
            int choice = Integer.parseInt(sc.nextLine().trim());
            if (choice >= 1 && choice <= matches.size()) return Optional.of(matches.get(choice - 1));
        } catch (NumberFormatException ignored) {}
        return Optional.empty();
    }
}
