package com.checkx.ui;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.domain.Stats;
import com.checkx.infrastructure.JsonHabitRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive shell (REPL) for CheckX.
 * Unix-style command interface.
 */
public class InteractiveShell {

    private final HabitRepository repository;
    private final Scanner scanner;
    private boolean running;

    private static final String[] ICON_OPTIONS = {
        "🏋 ", "📚", "💻", "🥗", "🏠", "🎯"
    };

    public InteractiveShell() {
        this.repository = new JsonHabitRepository();
        this.scanner = new Scanner(System.in);
        this.running = true;

    }

    public void start() {
        ConsoleColors.clearScreen();
        ConsoleColors.printBanner();
        
        System.out.println(ConsoleColors.muted("  Type 'help' for available commands\n"));
        
        while (running) {
            System.out.print(ConsoleColors.command(""));
            String input = scanner.nextLine().trim();
            
            if (!input.isEmpty()) {
                handleCommand(input);
            }
        }
        
        scanner.close();
        ConsoleColors.showGoodbye();
    }

    private void handleCommand(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "daily", "today" -> showDaily();
            case "done", "complete" -> completeHabit(args);
            case "add", "new" -> addHabit(args);
            case "stats", "statistics" -> showStats(args);
            case "list", "habits", "all" -> listHabits();
            case "edit", "rename" -> editHabit(args);
            case "undone", "undo" -> undoneHabit(args);
            case "delete", "remove", "rm" -> deleteHabit(args);
            case "help", "?" -> showHelp();
            case "clear", "cls" -> ConsoleColors.clearScreen();
            case "exit", "quit", "q" -> exit();
            default -> System.out.println(ConsoleColors.error("  Unknown command: " + command + ". Type 'help' for available commands."));
        }
    }

    private void showDaily() {
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println(ConsoleColors.warning("  No habits yet. Use 'add [name]' to create one."));
            System.out.println();
            return;
        }

        System.out.println();
        ConsoleColors.printSeparator();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        System.out.println(ConsoleColors.title("  📅 " + today));
        ConsoleColors.printLine();

        for (Habit habit : habits) {
            String checkbox = habit.isCompletedToday() 
                ? ConsoleColors.habitCompleted(habit.getIcon() + " " + habit.getName())
                : ConsoleColors.habitPending(habit.getIcon() + " " + habit.getName());
            
            String streakInfo = habit.getStreak() > 0 
                ? ConsoleColors.streak(habit.getStreak() + "d")
                : ConsoleColors.muted("0d");
            
            int padding = 50 - habit.getName().length();
            System.out.println("  " + checkbox + " ".repeat(Math.max(1, padding)) + streakInfo);
        }

        ConsoleColors.printLine();
        Stats stats = new Stats(habits);
        System.out.println("  " + ConsoleColors.info("Progress: ") + 
                          ConsoleColors.bold(stats.getCompletedToday() + "/" + stats.getTotalHabits()) +
                          ConsoleColors.muted(" (" + (int)stats.getCompletionPercentage() + "%)"));
        System.out.println("  " + ConsoleColors.muted(stats.getMotivationalMessage()));
        ConsoleColors.printSeparator();
        System.out.println();
    }

    private void completeHabit(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: done [habit name]"));
            System.out.println(ConsoleColors.muted("  Example: done exercise"));
            System.out.println();
            return;
        }

        var habitOpt = repository.findByName(args);

        if (habitOpt.isEmpty()) {
            System.out.println(ConsoleColors.error("  Habit not found: " + args));
            System.out.println(ConsoleColors.muted("  Tip: Use 'list' to see all habits"));
            System.out.println();
            return;
        }

        Habit habit = habitOpt.get();

        if (habit.isCompletedToday()) {
            System.out.println(ConsoleColors.warning("  " + habit.getIcon() + " " + habit.getName() + " already completed today"));
            System.out.println();
            return;
        }

        habit.complete();
        repository.save(habit);

        System.out.println(ConsoleColors.success("  ✓ " + habit.getIcon() + " " + habit.getName() + " completed!"));
        System.out.println("  " + ConsoleColors.streak(habit.getStreak() + " day streak!"));
        System.out.println();
    }

    private void addHabit(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: add [habit name]"));
            System.out.println(ConsoleColors.muted("  Example: add exercise"));
            System.out.println();
            return;
        }

        if (repository.findByName(args).isPresent()) {
            System.out.println(ConsoleColors.error("  Habit already exists: " + args));
            System.out.println();
            return;
        }

        String name = args.substring(0, 1).toUpperCase() + args.substring(1);
        String icon = promptIconSelection();

        Habit habit = new Habit(name, icon);
        repository.save(habit);

        System.out.println(ConsoleColors.success("  ✓ Added: " + icon + " " + name));
        System.out.println(ConsoleColors.muted("  Complete it with: done " + args.toLowerCase()));
        System.out.println();
    }

    private String promptIconSelection() {
        System.out.println();
        System.out.print("  " + ConsoleColors.info("Choose an icon: "));
        for (int i = 0; i < ICON_OPTIONS.length; i++) {
            System.out.print((i + 1) + ")  " + ICON_OPTIONS[i] + "  ");
        }
        System.out.println();
        System.out.print("  " + ConsoleColors.muted("Enter number (1-" + ICON_OPTIONS.length + ") or paste your own icon: "));
        System.out.print(ConsoleColors.command(""));

        String input = scanner.nextLine().trim();

        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= ICON_OPTIONS.length) {
                return ICON_OPTIONS[choice - 1];
            }
        } catch (NumberFormatException ignored) {
        }

        if (!input.isEmpty()) {
            return input;
        }

        return ICON_OPTIONS[0];
    }

    private void showStats(String args) {
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println(ConsoleColors.warning("  No habits yet. Use 'add [name]' to create one."));
            System.out.println();
            return;
        }

        Stats stats = new Stats(habits);

        System.out.println();
        ConsoleColors.printSeparator();
        
        String title = switch (args.toLowerCase()) {
            case "daily", "today" -> "📊 Today's Statistics";
            case "all", "total" -> "📊 All-Time Statistics";
            default -> "📊 Statistics";
        };
        
        System.out.println(ConsoleColors.title("  " + title));
        ConsoleColors.printLine();

        System.out.println("  " + ConsoleColors.info("Total habits:     ") + 
                          ConsoleColors.bold(String.valueOf(stats.getTotalHabits())));
        System.out.println("  " + ConsoleColors.info("Avg streak:       ") + 
                          ConsoleColors.bold(stats.getAverageStreak() + "d"));
        System.out.println("  " + ConsoleColors.info("Best streak:      ") + 
                          ConsoleColors.streak(stats.getBestStreak() + "d 🏆"));
        System.out.println("  " + ConsoleColors.info("Completed today:  ") + 
                          ConsoleColors.bold(stats.getCompletedToday() + "/" + stats.getTotalHabits()) +
                          ConsoleColors.muted(" (" + (int)stats.getCompletionPercentage() + "%)"));

        System.out.println();
        System.out.print("  ");
        ConsoleColors.progressBar(stats.getCompletedToday(), stats.getTotalHabits(), "Daily progress");

        Habit topHabit = stats.getTopHabit();
        if (topHabit != null && topHabit.getStreak() > 0) {
            System.out.println("  " + ConsoleColors.info("Top habit:        ") + 
                              ConsoleColors.bold(topHabit.getIcon() + " " + topHabit.getName()));
        }

        ConsoleColors.printSeparator();
        System.out.println();
    }

    private void listHabits() {
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println(ConsoleColors.warning("  No habits yet. Use 'add [name]' to create one."));
            System.out.println();
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
            
            String status = habit.isCompletedToday() 
                ? ConsoleColors.success(" ✓")
                : ConsoleColors.muted(" ");
            
            System.out.println(number + icon + " " + name + status + " - " + streak);
        }

        System.out.println();
        System.out.println("  " + ConsoleColors.muted("Total: " + habits.size() + " habits"));
        System.out.println();
    }

    private void editHabit(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: edit [habit name]"));
            System.out.println(ConsoleColors.muted("  Example: edit exercise"));
            System.out.println();
            return;
        }

        var habitOpt = repository.findByName(args);

        if (habitOpt.isEmpty()) {
            System.out.println(ConsoleColors.error("  Habit not found: " + args));
            System.out.println(ConsoleColors.muted("  Tip: Use 'list' to see all habits"));
            System.out.println();
            return;
        }

        Habit habit = habitOpt.get();

        System.out.println();
        System.out.println("  " + ConsoleColors.info("Editing: ") + habit.getIcon() + " " + ConsoleColors.bold(habit.getName()));
        System.out.println();
        System.out.println("  " + ConsoleColors.bold("What to edit?"));
        System.out.println("    1) Name");
        System.out.println("    2) Icon");
        System.out.println("    3) Both");
        System.out.println();
        System.out.print("  " + ConsoleColors.muted("Choice (1-3): "));
        System.out.print(ConsoleColors.command(""));

        String choice = scanner.nextLine().trim();

        if (choice.equals("1") || choice.equals("3")) {
            System.out.print("  " + ConsoleColors.muted("New name: "));
            System.out.print(ConsoleColors.command(""));
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);
                habit.setName(newName);
            }
        }

        if (choice.equals("2") || choice.equals("3")) {
            String newIcon = promptIconSelection();
            habit.setIcon(newIcon);
        }

        if (choice.equals("1") || choice.equals("2") || choice.equals("3")) {
            repository.save(habit);
            System.out.println(ConsoleColors.success("  ✓ Updated: " + habit.getIcon() + " " + habit.getName()));
        } else {
            System.out.println(ConsoleColors.muted("  Cancelled"));
        }
        System.out.println();
    }

    private void undoneHabit(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: undone [habit name]"));
            System.out.println(ConsoleColors.muted("  Example: undone exercise"));
            System.out.println();
            return;
        }

        var habitOpt = repository.findByName(args);

        if (habitOpt.isEmpty()) {
            System.out.println(ConsoleColors.error("  Habit not found: " + args));
            System.out.println(ConsoleColors.muted("  Tip: Use 'list' to see all habits"));
            System.out.println();
            return;
        }

        Habit habit = habitOpt.get();

        if (!habit.isCompletedToday()) {
            System.out.println(ConsoleColors.warning("  " + habit.getIcon() + " " + habit.getName() + " is not completed today"));
            System.out.println();
            return;
        }

        int oldStreak = habit.getStreak();
        habit.uncomplete();
        repository.save(habit);

        System.out.println(ConsoleColors.success("  ↩ " + habit.getIcon() + " " + habit.getName() + " marked as not completed"));
        System.out.println("  " + ConsoleColors.muted("Streak: " + oldStreak + "d → " + habit.getStreak() + "d"));
        System.out.println();
    }

    private void deleteHabit(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: delete [habit name]"));
            System.out.println(ConsoleColors.muted("  Example: delete exercise"));
            System.out.println();
            return;
        }

        var habitOpt = repository.findByName(args);

        if (habitOpt.isEmpty()) {
            System.out.println(ConsoleColors.error("  Habit not found: " + args));
            System.out.println();
            return;
        }

        Habit habit = habitOpt.get();
        
        System.out.print(ConsoleColors.warning("  Delete " + habit.getIcon() + " " + 
                                               habit.getName() + "? (y/N) "));
        System.out.print(ConsoleColors.command(""));
        String confirm = scanner.nextLine().trim().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            repository.delete(habit.getId());
            System.out.println(ConsoleColors.success("  ✓ Deleted: " + habit.getIcon() + " " + habit.getName()));
        } else {
            System.out.println(ConsoleColors.muted("  Cancelled"));
        }
        System.out.println();
    }

    private void showHelp() {
        System.out.println();
        ConsoleColors.printSeparator();
        System.out.println(ConsoleColors.title("  CHECKX COMMANDS"));
        ConsoleColors.printLine();
        
        System.out.println("  " + ConsoleColors.bold("Viewing:"));
        System.out.println("    " + ConsoleColors.info("daily") + ", " + ConsoleColors.info("today") + 
                         "          Show today's habits");
        System.out.println("    " + ConsoleColors.info("list") + ", " + ConsoleColors.info("habits") + ", " + 
                         ConsoleColors.info("all") + "     List all habits");
        System.out.println("    " + ConsoleColors.info("stats") + "                 Show statistics");
        System.out.println("    " + ConsoleColors.info("stats daily") + "           Today's stats");
        System.out.println("    " + ConsoleColors.info("stats all") + "             All-time stats");
        
        System.out.println();
        System.out.println("  " + ConsoleColors.bold("Managing:"));
        System.out.println("    " + ConsoleColors.info("add [name]") + "            Add new habit");
        System.out.println("    " + ConsoleColors.info("done [name]") + "           Complete a habit");
        System.out.println("    " + ConsoleColors.info("undone [name]") + "         Revert a completion");
        System.out.println("    " + ConsoleColors.info("edit [name]") + "           Edit name or icon");
        System.out.println("    " + ConsoleColors.info("delete [name]") + "         Delete a habit");
        
        System.out.println();
        System.out.println("  " + ConsoleColors.bold("Other:"));
        System.out.println("    " + ConsoleColors.info("help") + ", " + ConsoleColors.info("?") + 
                         "               Show this help");
        System.out.println("    " + ConsoleColors.info("clear") + ", " + ConsoleColors.info("cls") + 
                         "            Clear screen");
        System.out.println("    " + ConsoleColors.info("exit") + ", " + ConsoleColors.info("quit") + ", " + 
                         ConsoleColors.info("q") + "         Exit CheckX");
        
        ConsoleColors.printSeparator();
        
        System.out.println();
        System.out.println(ConsoleColors.muted("  Examples:"));
        System.out.println(ConsoleColors.muted("    $ add exercise"));
        System.out.println(ConsoleColors.muted("    $ done exercise"));
        System.out.println(ConsoleColors.muted("    $ daily"));
        System.out.println(ConsoleColors.muted("    $ stats all"));
        System.out.println();
    }

    private void exit() {
        running = false;
    }
}
