package com.checkx.commands;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.ui.ConsoleColors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Scanner;

/**
 * Adds a new habit.
 */
@Command(
    name = "add",
    description = "Add a new habit"
)
public class AddCommand implements Runnable {

    @Parameters(
        index = "0..*",
        description = "Name of the habit",
        arity = "1..*"
    )
    private String[] habitName;

    private final HabitRepository repository = new JsonHabitRepository();

    private static final String[] ICON_OPTIONS = {
        "🏋️", "📚", "💻", "🥗", "🏠", "🎯"
    };

    @Override
    public void run() {
        if (habitName == null || habitName.length == 0) {
            System.out.println(ConsoleColors.error("Usage: checkx add [habit name]"));
            return;
        }

        String raw = String.join(" ", habitName);
        String name;
        String comment = null;

        if (raw.contains(",")) {
            String[] parts = raw.split(",", 2);
            name = parts[0].trim();
            comment = parts[1].trim();
        } else {
            name = raw;
        }

        if (repository.findByName(name).isPresent()) {
            System.out.println(ConsoleColors.error("Habit already exists: " + name));
            return;
        }

        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        System.out.print("Choose an icon: ");
        for (int i = 0; i < ICON_OPTIONS.length; i++) {
            System.out.print((i + 1) + ")  " + ICON_OPTIONS[i] + "  ");
        }
        System.out.println();
        System.out.print("Enter number (1-" + ICON_OPTIONS.length + ") or paste your own icon: ");

        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine().trim();
        String icon = ICON_OPTIONS[0];

        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= ICON_OPTIONS.length) {
                icon = ICON_OPTIONS[choice - 1];
            }
        } catch (NumberFormatException e) {
            if (!input.isEmpty()) {
                icon = input;
            }
        }

        Habit habit = new Habit(name, icon, comment);
        repository.save(habit);

        System.out.println(ConsoleColors.success("✓ Added new habit: " + icon + " " + name));
        if (comment != null) {
            System.out.println(ConsoleColors.muted("   // " + comment));
        }
        System.out.println(ConsoleColors.muted("Complete it today with: checkx done " + name.toLowerCase()));
    }
}
