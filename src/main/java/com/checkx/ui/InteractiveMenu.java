package com.checkx.ui;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.domain.Stats;
import com.checkx.infrastructure.JsonHabitRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

/**
 * Interactive menu mode for CheckX.
 * Keeps the application running with a menu of options.
 */
public class InteractiveMenu {

    private final HabitRepository repository;
    private final Scanner scanner;
    private boolean running;

    private static final String[] DEFAULT_ICONS = {
        "🎯", "⚡", "🌟", "💎", "🚀", "🎨", "🎵", "🏋️",
        "📚", "💻", "🧘", "🏃", "🥗", "💧", "🌱", "✨"
    };

    public InteractiveMenu() {
        this.repository = new JsonHabitRepository();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public void start() {
        ConsoleColors.clearScreen();
        ConsoleColors.printBanner();
        
        while (running) {
            showMenu();
            handleMenuChoice();
        }
        
        scanner.close();
        ConsoleColors.showGoodbye();
    }

    private void showMenu() {
        ConsoleColors.clearScreen();
        System.out.println();
        ConsoleColors.printSeparator();
        System.out.println(ConsoleColors.title("  CHECKX MENU"));
        ConsoleColors.printLine();
        
        System.out.println("  " + ConsoleColors.info("1.") + " View today's habits");
        System.out.println("  " + ConsoleColors.info("2.") + " Complete a habit");
        System.out.println("  " + ConsoleColors.info("3.") + " Add new habit");
        System.out.println("  " + ConsoleColors.info("4.") + " View statistics");
        System.out.println("  " + ConsoleColors.info("5.") + " List all habits");
        System.out.println("  " + ConsoleColors.info("6.") + " Delete a habit");
        System.out.println("  " + ConsoleColors.error("0.") + " Exit");
        
        ConsoleColors.printSeparator();
        System.out.print(ConsoleColors.command(""));
    }

    private void handleMenuChoice() {
        try {
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                return;
            }

            int choice = Integer.parseInt(input);

            switch (choice) {
                case 1 -> showDaily();
                case 2 -> completeHabit();
                case 3 -> addHabit();
                case 4 -> showStats();
                case 5 -> listHabits();
                case 6 -> deleteHabit();
                case 0 -> exit();
                default -> System.out.println(ConsoleColors.error("\n  ✗ Invalid option. Please select 0-6."));
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.error("\n  ✗ Please enter a number."));
        }
    }

    private void showDaily() {
        ConsoleColors.clearScreen();
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println();
            System.out.println(ConsoleColors.warning("  No habits yet!"));
            System.out.println();
            pressEnterToContinue();
            return;
        }

        System.out.println();
        ConsoleColors.printSeparator();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        System.out.println(ConsoleColors.title("  📅 " + today));
        ConsoleColors.printLine();

        for (Habit habit : habits) {
            String habitLine = formatHabitLine(habit);
            System.out.println("  " + habitLine);
        }

        ConsoleColors.printLine();
        Stats stats = new Stats(habits);
        System.out.println("  " + ConsoleColors.info("Progress: ") + 
                          ConsoleColors.bold(stats.getCompletedToday() + "/" + stats.getTotalHabits()) +
                          ConsoleColors.muted(" (" + (int)stats.getCompletionPercentage() + "%)"));
        System.out.println("  " + ConsoleColors.muted(stats.getMotivationalMessage()));
        ConsoleColors.printSeparator();
        
        pressEnterToContinue();
    }

    private void completeHabit() {
        ConsoleColors.clearScreen();
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println();
            System.out.println(ConsoleColors.warning("  No habits to complete!"));
            System.out.println();
            pressEnterToContinue();
            return;
        }

        // Show habits with numbers
        System.out.println();
        System.out.println(ConsoleColors.title("  Select habit to complete:"));
        System.out.println();
        for (int i = 0; i < habits.size(); i++) {
            Habit h = habits.get(i);
            String status = h.isCompletedToday() ? ConsoleColors.success("✓") : " ";
            System.out.println("  " + ConsoleColors.info((i + 1) + ".") + " [" + status + "] " + 
                             h.getIcon() + " " + h.getName() + " " + ConsoleColors.muted("(" + h.getStreak() + "d)"));
        }
        System.out.println("  " + ConsoleColors.muted("0. Cancel"));
        
        System.out.println();
        System.out.print(ConsoleColors.command(""));
        String input = scanner.nextLine().trim();

        try {
            int choice = Integer.parseInt(input);
            
            if (choice == 0) {
                return;
            }
            
            if (choice < 1 || choice > habits.size()) {
                System.out.println(ConsoleColors.error("\n  ✗ Invalid selection."));
                pressEnterToContinue();
                return;
            }

            Habit habit = habits.get(choice - 1);

            if (habit.isCompletedToday()) {
                System.out.println(ConsoleColors.warning("\n  ⚠ " + habit.getIcon() + " " + 
                                                        habit.getName() + " is already completed today!"));
            } else {
                habit.complete();
                repository.save(habit);
                System.out.println(ConsoleColors.success("\n  ✓ " + habit.getIcon() + " " + 
                                                         habit.getName() + " completed!"));
                System.out.println("  " + ConsoleColors.streak(habit.getStreak() + " day streak!"));
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.error("\n  ✗ Invalid input."));
        }
        
        pressEnterToContinue();
    }

    private void addHabit() {
        ConsoleColors.clearScreen();
        System.out.println();
        System.out.println(ConsoleColors.title("  Add new habit"));
        System.out.println();
        System.out.print(ConsoleColors.command(""));
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println(ConsoleColors.error("\n  ✗ Habit name cannot be empty."));
            pressEnterToContinue();
            return;
        }

        // Check if exists
        if (repository.findByName(name).isPresent()) {
            System.out.println(ConsoleColors.error("\n  ✗ Habit already exists: " + name));
            pressEnterToContinue();
            return;
        }

        // Capitalize
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        // Random icon
        String icon = DEFAULT_ICONS[new Random().nextInt(DEFAULT_ICONS.length)];

        Habit habit = new Habit(name, icon);
        repository.save(habit);

        System.out.println(ConsoleColors.success("\n  ✓ Added: " + icon + " " + name));
        pressEnterToContinue();
    }

    private void showStats() {
        ConsoleColors.clearScreen();
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println();
            System.out.println(ConsoleColors.warning("  No habits to show stats for!"));
            System.out.println();
            pressEnterToContinue();
            return;
        }

        Stats stats = new Stats(habits);

        System.out.println();
        ConsoleColors.printSeparator();
        System.out.println(ConsoleColors.title("  📊 Statistics"));
        ConsoleColors.printLine();

        System.out.println("  " + ConsoleColors.info("Total habits:     ") + 
                          ConsoleColors.bold(String.valueOf(stats.getTotalHabits())));
        System.out.println("  " + ConsoleColors.info("Avg streak:       ") + 
                          ConsoleColors.bold(stats.getAverageStreak() + "d"));
        System.out.println("  " + ConsoleColors.info("Best streak:      ") + 
                          ConsoleColors.streak(stats.getBestStreak() + "d 🏆"));
        System.out.println("  " + ConsoleColors.info("Today:            ") + 
                          ConsoleColors.bold(stats.getCompletedToday() + "/" + stats.getTotalHabits()) +
                          ConsoleColors.muted(" (" + (int)stats.getCompletionPercentage() + "%)"));

        System.out.println();
        System.out.print("  ");
        ConsoleColors.progressBar(stats.getCompletedToday(), stats.getTotalHabits(), "Progress");

        Habit topHabit = stats.getTopHabit();
        if (topHabit != null && topHabit.getStreak() > 0) {
            System.out.println();
            System.out.println("  " + ConsoleColors.info("Top habit:        ") + 
                              ConsoleColors.bold(topHabit.getIcon() + " " + topHabit.getName()));
        }

        ConsoleColors.printSeparator();
        pressEnterToContinue();
    }

    private void listHabits() {
        ConsoleColors.clearScreen();
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println();
            System.out.println(ConsoleColors.warning("  No habits yet!"));
            System.out.println();
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println(ConsoleColors.title("  📋 All Habits"));
        System.out.println();

        for (int i = 0; i < habits.size(); i++) {
            Habit habit = habits.get(i);
            String number = ConsoleColors.muted(String.format("  %2d. ", i + 1));
            String icon = habit.getIcon();
            String name = ConsoleColors.bold(habit.getName());
            String streak = habit.getStreak() > 0 
                ? ConsoleColors.streak(habit.getStreak() + "d")
                : ConsoleColors.muted("0d");
            
            System.out.println(number + icon + " " + name + " - " + streak);
        }

        System.out.println();
        System.out.println("  " + ConsoleColors.muted("Total: " + habits.size() + " habits"));
        
        pressEnterToContinue();
    }

    private void deleteHabit() {
        ConsoleColors.clearScreen();
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println();
            System.out.println(ConsoleColors.warning("  No habits to delete!"));
            System.out.println();
            pressEnterToContinue();
            return;
        }

        System.out.println();
        System.out.println(ConsoleColors.title("  Select habit to delete:"));
        System.out.println();
        for (int i = 0; i < habits.size(); i++) {
            Habit h = habits.get(i);
            System.out.println("  " + ConsoleColors.info((i + 1) + ".") + " " + 
                             h.getIcon() + " " + h.getName());
        }
        System.out.println("  " + ConsoleColors.muted("0. Cancel"));
        
        System.out.println();
        System.out.print(ConsoleColors.command(""));
        String input = scanner.nextLine().trim();

        try {
            int choice = Integer.parseInt(input);
            
            if (choice == 0) {
                return;
            }
            
            if (choice < 1 || choice > habits.size()) {
                System.out.println(ConsoleColors.error("\n  ✗ Invalid selection."));
                pressEnterToContinue();
                return;
            }

            Habit habit = habits.get(choice - 1);
            
            System.out.println();
            System.out.print(ConsoleColors.warning("  Delete " + habit.getIcon() + " " + 
                                                   habit.getName() + "? (y/N) "));
            System.out.print(ConsoleColors.command(""));
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                repository.delete(habit.getId());
                System.out.println(ConsoleColors.success("\n  ✓ Deleted: " + habit.getIcon() + " " + habit.getName()));
            } else {
                System.out.println(ConsoleColors.muted("\n  Cancelled."));
            }
        } catch (NumberFormatException e) {
            System.out.println(ConsoleColors.error("\n  ✗ Invalid input."));
        }
        
        pressEnterToContinue();
    }

    private void exit() {
        running = false;
    }

    private String formatHabitLine(Habit habit) {
        String checkbox = habit.isCompletedToday() 
            ? ConsoleColors.habitCompleted(habit.getIcon() + " " + habit.getName())
            : ConsoleColors.habitPending(habit.getIcon() + " " + habit.getName());
        
        String streakInfo = habit.getStreak() > 0 
            ? ConsoleColors.streak(habit.getStreak() + "d")
            : ConsoleColors.muted("0d");
        
        int padding = 50 - habit.getName().length();
        return checkbox + " ".repeat(Math.max(1, padding)) + streakInfo;
    }

    private void pressEnterToContinue() {
        System.out.println();
        System.out.print(ConsoleColors.muted("  Press Enter to continue..."));
        scanner.nextLine();
    }
}
