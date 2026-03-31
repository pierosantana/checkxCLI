package com.checkx.ui;

/**
 * Console color utilities for CheckX using pure ANSI escape codes.
 * No external dependencies - pure Java.
 */
public class ConsoleColors {

    // ANSI Color Codes
    private static final String RESET = "\u001B[0m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    // Bright colors
    private static final String BRIGHT_BLACK = "\u001B[90m";
    private static final String BRIGHT_RED = "\u001B[91m";
    private static final String BRIGHT_GREEN = "\u001B[92m";
    private static final String BRIGHT_YELLOW = "\u001B[93m";
    private static final String BRIGHT_BLUE = "\u001B[94m";
    private static final String BRIGHT_MAGENTA = "\u001B[95m";
    private static final String BRIGHT_CYAN = "\u001B[96m";
    private static final String BRIGHT_WHITE = "\u001B[97m";

    // Background colors
    private static final String BG_BLACK = "\u001B[40m";
    private static final String BG_RED = "\u001B[41m";
    private static final String BG_GREEN = "\u001B[42m";
    private static final String BG_YELLOW = "\u001B[43m";
    private static final String BG_BLUE = "\u001B[44m";
    private static final String BG_MAGENTA = "\u001B[45m";
    private static final String BG_CYAN = "\u001B[46m";
    private static final String BG_WHITE = "\u001B[47m";
    public static final String STRIKETHROUGH = "\u001B[9m";

    // Text attributes
    private static final String BOLD = "\u001B[1m";
    private static final String UNDERLINE = "\u001B[4m";

    // Text colors
    public static String success(String text) {
        return GREEN + text + RESET;
    }

    public static String successBar(String text) {
        return BRIGHT_GREEN + text + RESET;
    }

    public static String error(String text) {
        return RED + text + RESET;
    }

    public static String warning(String text) {
        return YELLOW + text + RESET;
    }

    public static String info(String text) {
        return CYAN + text + RESET;
    }

    public static String highlight(String text) {
        return BRIGHT_MAGENTA + text + RESET;
    }

    public static String title(String text) {
        return BRIGHT_WHITE + BOLD + text + RESET;
    }

    public static String muted(String text) {
        return BRIGHT_BLACK + text + RESET;
    }

    // CheckX specific colors
    public static String streak(String text) {
        return BRIGHT_GREEN + BOLD + "▲ " + text + RESET;
    }

    public static String habitCompleted(String text) {
        return BRIGHT_GREEN + "[✓] " +  BRIGHT_WHITE + text + RESET;
    }

    public static String habitPending(String text) {
        return WHITE + "[ ] " + BRIGHT_WHITE + text + RESET;
    }

    public static String command(String text) {
        return BRIGHT_GREEN + "~$ " + BRIGHT_WHITE + text + RESET;
    }

    // Todo specific colors
    public static String todoCompleted(String text) {
        return BRIGHT_GREEN + "[✓] " + STRIKETHROUGH +  BRIGHT_WHITE + text + RESET;
    }

    // Tab bar
    public static String tabActive(String text) {
        return BOLD + GREEN + UNDERLINE + " " + text + " " + RESET;
    }

    public static String tabInactive(String text) {
        return BRIGHT_BLACK + " " + text + " " + RESET;
    }

    // Backgrounds
    public static String successBg(String text) {
        return BG_GREEN + BLACK + " " +   text + " " + RESET;
    }

    public static String errorBg(String text) {
        return BG_RED + WHITE + " " + text + " " + RESET;
    }

    public static String warningBg(String text) {
        return BG_YELLOW + BLACK + " " + text + " " + RESET;
    }

    public static String infoBg(String text) {
        return BG_CYAN + WHITE + " " + text + " " + RESET;
    }

    // Special effects
    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static String underline(String text) {
        return UNDERLINE + text + RESET;
    }

    // Clear screen - Works on Windows, Mac, and Linux
    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Mac/Linux - Use ANSI escape codes
                System.out.print("\033[H\033[2J");
                System.out.flush();
                
                // Alternative for terminals that need it
                System.out.print("\033c");
                System.out.flush();
            }
        } catch (Exception e) {
            // Fallback: print blank lines if clearing fails
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    // Progress bar
    public static void progressBar(int current, int total, String label) {
        int barLength = 40;
        int progress = (int) ((double) current / total * barLength);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");

        int percentage = (int) ((double) current / total * 100);
        System.out.println(info(label + ": ") + success(bar.toString()) +
                " " + highlight(percentage + "%"));
    }

    // Banner
    public static void printBanner() {
        // Parte "CHECK" (blanco) | Parte "X" (rojo)
        String[][] banner = {
                {" ██████ ██   ██ ███████  ██████ ██   ██ ", "██   ██"},
                {"██      ██   ██ ██      ██      ██  ██   ", "██ ██ "},
                {"██      ███████ █████   ██      █████     ", "███  "},
                {"██      ██   ██ ██      ██      ██  ██   ", "██ ██ "},
                {" ██████ ██   ██ ███████  ██████ ██   ██ ", "██   ██"}
        };

        System.out.println();
        for (int i = 0; i < banner.length; i++) {
            // "CHECK" en blanco + "X" en rojo
            System.out.println(BRIGHT_RED + banner[i][0] + BRIGHT_BLACK + banner[i][1] + RESET);
        }
        System.out.println(muted("     Your terminal app for habits & stay focus."));
        System.out.println();
    }

    // Decorative dividers
    public static void printSeparator() {
        System.out.println(info("═".repeat(60)));
    }

    public static void printLine() {
        System.out.println(muted("─".repeat(60)));
    }

    // Welcome message
    public static void showWelcome() {
        clearScreen();
        printBanner();
        System.out.println(muted("  Type 'checkx help' to see available commands\n"));
    }

    // Goodbye message
    public static void showGoodbye() {
        System.out.println();
        System.out.println(success("  ╔═══════════════════════════════════════╗"));
        System.out.println(success("  ║                                       ║"));
        System.out.println(success("  ║   ✓ Keep building great habits!       ║"));
        System.out.println(success("  ║   See you tomorrow!                   ║"));
        System.out.println(success("  ║                                       ║"));
        System.out.println(success("  ╚═══════════════════════════════════════╝"));
        System.out.println();
    }
}
