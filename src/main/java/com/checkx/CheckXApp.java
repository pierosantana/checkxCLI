package com.checkx;

import com.checkx.commands.CheckXCommand;
import com.checkx.ui.InteractiveShell;
import picocli.CommandLine;

/**
 * Main entry point for CheckX application.
 * - No arguments: Interactive shell mode
 * - With arguments: CLI command mode
 */
public class CheckXApp {
    
    public static void main(String[] args) {
        if (args.length == 0) {
            // Interactive shell mode
            InteractiveShell shell = new InteractiveShell();
            shell.start();
        } else {
            // CLI mode
            int exitCode = new CommandLine(new CheckXCommand()).execute(args);
            System.exit(exitCode);
        }
    }
}
