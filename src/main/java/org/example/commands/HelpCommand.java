package org.example.commands;

import org.example.client.ConsoleReader;

import java.util.Map;

/**
 * Help command: displays descriptions of other commands.
 */
public class HelpCommand implements Command {
    private final ConsoleReader console;
    private final Map<String, Command> commandMap; // Needs access to the map of commands

    public HelpCommand(ConsoleReader console, Map<String, Command> commandMap) {
        this.console = console;
        this.commandMap = commandMap;
    }

    @Override
    public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) {
            console.printError("Help command doesn't take arguments.");
            return true;
        }
        console.println("--- Available Commands ---");
        if (commandMap.isEmpty()) {
            console.println("No commands registered.");
        } else {
            // Print description for each command in the map
            for (Command cmd : commandMap.values()) {
                console.println(cmd.getDescription());
            }
        }
        console.println("------------------------");
        return true;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "help : display help for available commands";
    }
}
