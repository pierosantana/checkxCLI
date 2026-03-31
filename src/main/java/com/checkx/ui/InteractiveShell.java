package com.checkx.ui;

import com.checkx.domain.Habit;
import com.checkx.domain.HabitRepository;
import com.checkx.domain.Stats;
import com.checkx.domain.TodoTask;
import com.checkx.infrastructure.JsonHabitRepository;
import com.checkx.infrastructure.JsonTodoRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive shell (REPL) for CheckX.
 * Unix-style command interface.
 */
public class InteractiveShell {

    private final HabitRepository repository;
    private final JsonTodoRepository todoRepository;
    private final Scanner scanner;
    private boolean running;

    private static final String[] ICON_OPTIONS = {
            "⭐️", "📚", "💻", "💪", "🥗", "🏠"
    };

    public InteractiveShell() {
        this.repository = new JsonHabitRepository();
        this.todoRepository = new JsonTodoRepository();
        this.scanner = new Scanner(System.in);
        this.running = true;
    }

    public void start() {
        ConsoleColors.clearScreen();
        ConsoleColors.printBanner();

        System.out.println(ConsoleColors.muted("  Type 'help' for available commands\n"));

        showDaily();

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
            case "todo" -> handleTodoCommand(args);
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

        int total = habits.size();
        LocalDate today = LocalDate.now();


        printTabBar("HABITS");

        String todayFormatted = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy"));
        System.out.println(ConsoleColors.title("  🗓️ " + todayFormatted));

        // Get Monday of current week
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);

        System.out.println();
        /*ConsoleColors.printSeparator();*/

        // Week calendar: Mon-Sun
        StringBuilder headerLine = new StringBuilder("  ");
        StringBuilder dataLine = new StringBuilder("  ");
        StringBuilder barLine = new StringBuilder("  ");

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            String label = day.format(DateTimeFormatter.ofPattern("EEE"));
            boolean isToday = day.equals(today);
            boolean isFuture = day.isAfter(today);

            int completed = 0;
            if (!isFuture) {
                if (isToday) {
                    for (Habit h : habits) if (h.isCompletedToday()) completed++;
                } else {
                    for (Habit h : habits) if (wasCompletedOn(h, day)) completed++;
                }
            }

            String num = String.valueOf(day.getDayOfMonth());
            String marker = isToday ? ConsoleColors.title("*") : " ";
            String block;
            if (isFuture) {
                block = ConsoleColors.muted("█");
            } else {
                block = progressBlock(completed, total);
            }

            headerLine.append(String.format(" %-7s", label));
            dataLine.append(String.format("%s%2s%s    ", marker, num, " "));
            barLine.append(String.format("%s%s%s%s    ", block, block, block,block));
        }

        System.out.println(headerLine);
        System.out.println(dataLine);
        System.out.println(barLine);
        System.out.println("");

        /*ConsoleColors.printLine();*/

        System.out.println(ConsoleColors.title("  Daily goals"));

        for (Habit habit : habits) {
            String checkbox = habit.isCompletedToday()
                ? ConsoleColors.habitCompleted(habit.getIcon() + " " + habit.getName())
                : ConsoleColors.habitPending(habit.getIcon() + " " + habit.getName());

            String streakInfo = habit.getStreak() > 0
                ? ConsoleColors.streak(habit.getStreak() + "d")
                : ConsoleColors.muted("0d");

            int padding = 35 - habit.getName().length();
            System.out.println("  " + checkbox + " ".repeat(Math.max(1, padding)) + streakInfo);

            if (habit.getComment() != null && !habit.getComment().isEmpty()) {
                System.out.println("        " + ConsoleColors.muted("// " + habit.getComment()));
            }
        }


        ConsoleColors.printLine();
        Stats stats = new Stats(habits);
        int completed = stats.getCompletedToday();
        int totalHabits = stats.getTotalHabits();
        int pct = (int) stats.getCompletionPercentage();

        int barLen = 40;
        int filled = totalHabits > 0 ? (completed * barLen / totalHabits) : 0;

        StringBuilder bar = new StringBuilder("  ");
        for (int i = 0; i < barLen; i++) {
            if (i < filled) {
                bar.append(pct >= 60 ? ConsoleColors.success("█") : ConsoleColors.warning("█"));
            } else {
                bar.append(ConsoleColors.muted("░"));
            }
        }
        bar.append("  ").append(ConsoleColors.bold(pct + "%"));

        System.out.println(bar);
        System.out.println("  " + ConsoleColors.muted("Progress: " + completed + "/" + totalHabits));
        /*ConsoleColors.printLine();*/
        System.out.println();
    }

    private void completeHabit(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: done [habit name]"));
            System.out.println(ConsoleColors.muted("  Example: done exercise"));
            System.out.println();
            return;
        }

        var habitOpt = resolveHabit(args);
        if (habitOpt.isEmpty()) return;

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

        String name;
        String comment = null;

        if (args.contains(",")) {
            String[] parts = args.split(",", 2);
            name = parts[0].trim();
            comment = parts[1].trim();
        } else {
            name = args;
        }

        if (repository.findByName(name).isPresent()) {
            System.out.println(ConsoleColors.error("  Habit already exists: " + name));
            System.out.println();
            return;
        }

        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        String icon = promptIconSelection();

        Habit habit = new Habit(name, icon, comment);
        repository.save(habit);

        System.out.println(ConsoleColors.success("  ✓ Added: " + icon + " " + name));
        if (comment != null) {
            System.out.println(ConsoleColors.muted("        // " + comment));
        }
        System.out.println(ConsoleColors.muted("  Complete it with: done " + name.toLowerCase()));
        System.out.println();
    }

    private String promptIconSelection() {
        System.out.println();
        System.out.print("  " + ConsoleColors.info("Choose an icon: "));
        for (int i = 0; i < ICON_OPTIONS.length; i++) {
            System.out.print((i + 1) +":" + ICON_OPTIONS[i] + "  ");
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

    private Optional<Habit> resolveHabit(String args) {
        // Try exact match first
        var exact = repository.findByName(args);
        if (exact.isPresent()) {
            return exact;
        }

        // Try partial match
        List<Habit> matches = repository.searchByName(args);

        if (matches.isEmpty()) {
            System.out.println(ConsoleColors.error("  Habit not found: " + args));
            System.out.println(ConsoleColors.muted("  Tip: Use 'list' to see all habits"));
            System.out.println();
            return Optional.empty();
        }

        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        }

        // Multiple matches - ask user to pick
        System.out.println(ConsoleColors.warning("  Multiple habits match '" + args + "':"));
        for (int i = 0; i < matches.size(); i++) {
            Habit h = matches.get(i);
            System.out.println("    " + (i + 1) + ") " + h.getIcon() + " " + h.getName());
        }
        System.out.print("  " + ConsoleColors.muted("Choose (1-" + matches.size() + "): "));
        System.out.print(ConsoleColors.command(""));

        String input = scanner.nextLine().trim();
        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= matches.size()) {
                return Optional.of(matches.get(choice - 1));
            }
        } catch (NumberFormatException ignored) {
        }

        System.out.println(ConsoleColors.muted("  Cancelled"));
        System.out.println();
        return Optional.empty();
    }

    private boolean wasCompletedOn(Habit habit, LocalDate date) {
        LocalDate last = habit.getLastCompletedDate();
        if (last == null) return false;

        // If last completed is exactly that date
        if (last.equals(date)) return true;

        // If last completed is after that date, check if streak covers it
        if (last.isAfter(date)) {
            long daysBetween = date.until(last).getDays();
            return habit.getStreak() > daysBetween;
        }

        return false;
    }

    private String progressBlock(int completed, int total) {
        if (total == 0 || completed == 0) return ConsoleColors.muted("█");
        if (completed == total) return ConsoleColors.successBar("█");
        return ConsoleColors.warning("█");
    }

    private void showStats(String args) {
        List<Habit> habits = repository.findAll();

        if (habits.isEmpty()) {
            System.out.println(ConsoleColors.warning("  No habits yet. Use 'add [name]' to create one."));
            System.out.println();
            return;
        }

        Stats stats = new Stats(habits);

        printTabBar("STATS");

        String todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy"));
        System.out.println(ConsoleColors.title("  🗓️ " + todayFormatted));
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

        var habitOpt = resolveHabit(args);
        if (habitOpt.isEmpty()) return;

        Habit habit = habitOpt.get();

        System.out.println();
        System.out.println("  " + ConsoleColors.info("Editing: ") + habit.getIcon() + " " + ConsoleColors.bold(habit.getName()));
        System.out.println();
        String currentComment = habit.getComment() != null ? habit.getComment() : "";
        if (!currentComment.isEmpty()) {
            System.out.println("        " + ConsoleColors.muted("// " + currentComment));
        }
        System.out.println();
        System.out.println("  " + ConsoleColors.bold("What to edit?"));
        System.out.println("    1) Name");
        System.out.println("    2) Icon");
        System.out.println("    3) Comment");
        System.out.println("    4) All");
        System.out.println();
        System.out.print("  " + ConsoleColors.muted("Choice (1-4): "));
        System.out.print(ConsoleColors.command(""));

        String choice = scanner.nextLine().trim();

        if (choice.equals("1") || choice.equals("4")) {
            System.out.print("  " + ConsoleColors.muted("New name: "));
            System.out.print(ConsoleColors.command(""));
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                newName = newName.substring(0, 1).toUpperCase() + newName.substring(1);
                habit.setName(newName);
            }
        }

        if (choice.equals("2") || choice.equals("4")) {
            String newIcon = promptIconSelection();
            habit.setIcon(newIcon);
        }

        if (choice.equals("3") || choice.equals("4")) {
            System.out.print("  " + ConsoleColors.muted("New comment (leave empty to remove): "));
            System.out.print(ConsoleColors.command(""));
            String newComment = scanner.nextLine().trim();
            habit.setComment(newComment.isEmpty() ? null : newComment);
        }

        if (choice.matches("[1-4]")) {
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

        var habitOpt = resolveHabit(args);
        if (habitOpt.isEmpty()) return;

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

        var habitOpt = resolveHabit(args);
        if (habitOpt.isEmpty()) return;

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
                         "          Show today's habits & weekly calendar");
        System.out.println("    " + ConsoleColors.info("list") + ", " + ConsoleColors.info("habits") + ", " +
                         ConsoleColors.info("all") + "     List all habits");
        System.out.println("    " + ConsoleColors.info("stats") + "                 Show statistics");
        System.out.println("    " + ConsoleColors.info("stats daily") + "           Today's stats");
        System.out.println("    " + ConsoleColors.info("stats all") + "             All-time stats");

        System.out.println();
        System.out.println("  " + ConsoleColors.bold("Habits:"));
        System.out.println("    " + ConsoleColors.info("add [name]") + "            Add new habit");
        System.out.println("    " + ConsoleColors.info("add [name], [comment]") + " Add habit with comment");
        System.out.println("    " + ConsoleColors.info("done [name]") + "           Complete a habit");
        System.out.println("    " + ConsoleColors.info("undone [name]") + "         Revert today's completion");
        System.out.println("    " + ConsoleColors.info("edit [name]") + "           Edit name, icon or comment");
        System.out.println("    " + ConsoleColors.info("delete [name]") + "         Delete a habit");

        System.out.println();
        System.out.println("  " + ConsoleColors.bold("Tasks:"));
        System.out.println("    " + ConsoleColors.info("todo") + "                  Show today's tasks");
        System.out.println("    " + ConsoleColors.info("todo add [task]") + "       Add a quick task (max 35 chars)");
        System.out.println("    " + ConsoleColors.info("todo done [task]") + "      Complete a task");
        System.out.println("    " + ConsoleColors.info("todo undone [task]") + "    Revert task completion");
        System.out.println("    " + ConsoleColors.info("todo del [task]") + "       Delete a task");
        System.out.println("    " + ConsoleColors.info("todo rm [task]") + "        Alias for del");

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
        System.out.println(ConsoleColors.muted("  Tips:"));
        System.out.println(ConsoleColors.muted("    - Partial names work: 'done ex' finds 'Exercise'"));
        System.out.println(ConsoleColors.muted("    - Comments use comma: add read, 30 min before bed"));
        System.out.println();
        System.out.println(ConsoleColors.muted("  Examples:"));
        System.out.println(ConsoleColors.muted("    $ add exercise"));
        System.out.println(ConsoleColors.muted("    $ add code, spring security 30min"));
        System.out.println(ConsoleColors.muted("    $ done exercise"));
        System.out.println(ConsoleColors.muted("    $ undone exercise"));
        System.out.println(ConsoleColors.muted("    $ edit ex"));
        System.out.println(ConsoleColors.muted("    $ daily"));
        System.out.println();
    }

    private void exit() {
        running = false;
    }

    // ── Tab Bar ──────────────────────────────────────────

    private void printTabBar(String active) {
        System.out.println();
        System.out.print("  ");
        System.out.print(active.equals("HABITS") ? ConsoleColors.tabActive("HABITS") : ConsoleColors.tabInactive("HABITS"));
        System.out.print("    ");
        System.out.print(active.equals("TODO") ? ConsoleColors.tabActive("TODO") : ConsoleColors.tabInactive("TODO"));
        System.out.print("    ");
        System.out.println(active.equals("STATS") ? ConsoleColors.tabActive("STATS") : ConsoleColors.tabInactive("STATS"));
        ConsoleColors.printLine();
    }

    // ── TODO Feature ─────────────────────────────────────

    private void handleTodoCommand(String args) {
        if (args.isEmpty()) {
            showTodo();
            return;
        }

        String[] parts = args.split("\\s+", 2);
        String subCommand = parts[0].toLowerCase();
        String subArgs = parts.length > 1 ? parts[1] : "";

        switch (subCommand) {
            case "add" -> addTodo(subArgs);
            case "done" -> completeTodo(subArgs);
            case "undone", "undo" -> undoneTodo(subArgs);
            case "del", "rm" -> deleteTodo(subArgs);
            default -> {
                // If no subcommand matched, treat the whole args as "add"
                addTodo(args);
            }
        }
    }

    private void showTodo() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<TodoTask> todayTasks = todoRepository.findByDate(today);
        List<TodoTask> yesterdayTasks = todoRepository.findByDate(yesterday);

        printTabBar("TODO");

        String todayFormatted = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy"));
        System.out.println(ConsoleColors.title("  \uD83D\uDDD3\uFE0F " + todayFormatted));
        System.out.println();

        if (todayTasks.isEmpty()) {
            System.out.println(ConsoleColors.muted("  No tasks for today. Use 'todo add [task]' to create one."));
        } else {
            for (TodoTask task : todayTasks) {
                String checkbox = task.isCompleted()
                        ? ConsoleColors.todoCompleted(task.getText())
                        : ConsoleColors.habitPending(task.getText());
                System.out.println("  " + checkbox);
            }
        }

        if (!yesterdayTasks.isEmpty()) {
            System.out.println();
            String yesterdayFormatted = yesterday.format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy"));
            System.out.println("  " + ConsoleColors.muted("── " + yesterdayFormatted + " ──"));
            System.out.println();

            for (TodoTask task : yesterdayTasks) {
                String checkbox = task.isCompleted()
                        ? ConsoleColors.todoCompleted(task.getText())
                        : ConsoleColors.habitPending(task.getText());
                System.out.println("  " + checkbox);
            }
        }

        ConsoleColors.printLine();
        System.out.println("  " + ConsoleColors.muted("todo add [task] \u00B7 todo done [name] \u00B7 todo del [name]"));
        System.out.println();
    }

    private void addTodo(String text) {
        if (text.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: todo add [task text]"));
            System.out.println(ConsoleColors.muted("  Example: todo add Buy groceries"));
            System.out.println();
            return;
        }

        if (text.length() > TodoTask.getMaxTextLength()) {
            System.out.println(ConsoleColors.error("  Task too long (" + text.length() + " chars). Max " + TodoTask.getMaxTextLength() + "."));
            System.out.println();
            return;
        }

        text = text.substring(0, 1).toUpperCase() + text.substring(1);
        TodoTask task = new TodoTask(text);
        todoRepository.save(task);

        System.out.println(ConsoleColors.success("  \u2713 Added: " + task.getText()));
        System.out.println();
    }

    private void completeTodo(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: todo done [task name]"));
            System.out.println();
            return;
        }

        var taskOpt = resolveTask(args);
        if (taskOpt.isEmpty()) return;

        TodoTask task = taskOpt.get();

        if (task.isCompleted()) {
            System.out.println(ConsoleColors.warning("  " + task.getText() + " already completed"));
            System.out.println();
            return;
        }

        task.complete();
        todoRepository.save(task);

        System.out.println(ConsoleColors.success("  \u2713 " + task.getText() + " completed!"));
        System.out.println();
    }

    private void undoneTodo(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: todo undone [task name]"));
            System.out.println();
            return;
        }

        var taskOpt = resolveTask(args);
        if (taskOpt.isEmpty()) return;

        TodoTask task = taskOpt.get();

        if (!task.isCompleted()) {
            System.out.println(ConsoleColors.warning("  " + task.getText() + " is not completed"));
            System.out.println();
            return;
        }

        task.uncomplete();
        todoRepository.save(task);

        System.out.println(ConsoleColors.success("  \u21A9 " + task.getText() + " marked as not completed"));
        System.out.println();
    }

    private void deleteTodo(String args) {
        if (args.isEmpty()) {
            System.out.println(ConsoleColors.error("  Usage: todo del [task name]"));
            System.out.println();
            return;
        }

        var taskOpt = resolveTask(args);
        if (taskOpt.isEmpty()) return;

        TodoTask task = taskOpt.get();

        System.out.print(ConsoleColors.warning("  Delete '" + task.getText() + "'? (y/N) "));
        System.out.print(ConsoleColors.command(""));
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            todoRepository.delete(task.getId());
            System.out.println(ConsoleColors.success("  \u2713 Deleted: " + task.getText()));
        } else {
            System.out.println(ConsoleColors.muted("  Cancelled"));
        }
        System.out.println();
    }

    private Optional<TodoTask> resolveTask(String args) {
        // Try exact match first
        var exact = todoRepository.findByText(args);
        if (exact.isPresent()) {
            return exact;
        }

        // Try partial match
        List<TodoTask> matches = todoRepository.searchByText(args);

        if (matches.isEmpty()) {
            System.out.println(ConsoleColors.error("  Task not found: " + args));
            System.out.println(ConsoleColors.muted("  Tip: Use 'todo' to see all tasks"));
            System.out.println();
            return Optional.empty();
        }

        if (matches.size() == 1) {
            return Optional.of(matches.get(0));
        }

        // Multiple matches - ask user to pick
        System.out.println(ConsoleColors.warning("  Multiple tasks match '" + args + "':"));
        for (int i = 0; i < matches.size(); i++) {
            TodoTask t = matches.get(i);
            String status = t.isCompleted() ? ConsoleColors.success("[✓]") : ConsoleColors.muted("[ ]");
            System.out.println("    " + (i + 1) + ") " + status + " " + t.getText());
        }
        System.out.print("  " + ConsoleColors.muted("Choose (1-" + matches.size() + "): "));
        System.out.print(ConsoleColors.command(""));

        String input = scanner.nextLine().trim();
        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= matches.size()) {
                return Optional.of(matches.get(choice - 1));
            }
        } catch (NumberFormatException ignored) {
        }

        System.out.println(ConsoleColors.muted("  Cancelled"));
        System.out.println();
        return Optional.empty();
    }
}
