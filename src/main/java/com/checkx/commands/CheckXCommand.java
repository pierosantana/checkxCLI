package com.checkx.commands;

import com.checkx.ui.ConsoleColors;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main CheckX command.
 * Entry point for all subcommands.
 */
@Command(
    name = "checkx",
    description = "Your terminal companion for habits & focus",
    version = "CheckX 1.0.0",
    mixinStandardHelpOptions = true,
    subcommands = {
        DailyCommand.class,
        DoneCommand.class,
        AddCommand.class,
        EditCommand.class,
        UndoneCommand.class,
        StatsCommand.class,
        ListCommand.class
    }
)
public class CheckXCommand implements Runnable {

    @Override
    public void run() {
        // When no subcommand is provided, show welcome
        ConsoleColors.showWelcome();
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CheckXCommand()).execute(args);
        System.exit(exitCode);
    }
}
