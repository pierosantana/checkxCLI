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
        var habitOpt = resolveHabit(name);
        if (habitOpt.isEmpty()) return;

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

    private Optional<Habit> resolveHabit(String query) {
        var exact = repository.findByName(query);
        if (exact.isPresent()) return exact;

        List<Habit> matches = repository.searchByName(query);
        if (matches.isEmpty()) {
            System.out.println(ConsoleColors.error("Habit not found: " + query));
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
