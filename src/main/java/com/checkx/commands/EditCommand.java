package com.checkx.commands;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.ui.ConsoleColors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.Scanner;

/**
 * Edits an existing habit (name and/or icon).
 */
@Command(
    name = "edit",
    description = "Edit a habit's name or icon"
)
public class EditCommand implements Runnable {

    @Parameters(
        index = "0..*",
        description = "Name of the habit to edit",
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
            System.out.println(ConsoleColors.error("Usage: checkx edit [habit name]"));
            return;
        }

        String name = String.join(" ", habitName);
        var habitOpt = repository.findByName(name);

        if (habitOpt.isEmpty()) {
            System.out.println(ConsoleColors.error("Habit not found: " + name));
            return;
        }

        Habit habit = habitOpt.get();
        Scanner sc = new Scanner(System.in);

        System.out.println("Editing: " + habit.getIcon() + " " + habit.getName());
        System.out.println();
        System.out.println("What to edit?");
        System.out.println("  1) Name");
        System.out.println("  2) Icon");
        System.out.println("  3) Both");
        System.out.print("Choice (1-3): ");

        String choice = sc.nextLine().trim();

        if (choice.equals("1") || choice.equals("3")) {
            System.out.print("New name: ");
            String newName = sc.nextLine().trim();
            if (!newName.isEmpty()) {
                newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);
                habit.setName(newName);
            }
        }

        if (choice.equals("2") || choice.equals("3")) {
            System.out.print("Choose an icon: ");
            for (int i = 0; i < ICON_OPTIONS.length; i++) {
                System.out.print((i + 1) + ")  " + ICON_OPTIONS[i] + "  ");
            }
            System.out.println();
            System.out.print("Enter number (1-" + ICON_OPTIONS.length + ") or paste your own icon: ");

            String input = sc.nextLine().trim();
            String icon = habit.getIcon();

            try {
                int iconChoice = Integer.parseInt(input);
                if (iconChoice >= 1 && iconChoice <= ICON_OPTIONS.length) {
                    icon = ICON_OPTIONS[iconChoice - 1];
                }
            } catch (NumberFormatException e) {
                if (!input.isEmpty()) {
                    icon = input;
                }
            }

            habit.setIcon(icon);
        }

        if (choice.equals("1") || choice.equals("2") || choice.equals("3")) {
            repository.save(habit);
            System.out.println(ConsoleColors.success("✓ Updated: " + habit.getIcon() + " " + habit.getName()));
        } else {
            System.out.println("Cancelled");
        }
    }
}
