package org.example.commands;
import org.example.utility.ConsoleReader;

import java.io.Console;



/**
 * Interface for all commands. Defines the basic structure.
 */
public interface Command {
    /**
     * Executes the command's logic.
     * @param args Arguments provided by the user (can be null or empty).
     * @return true if the application should continue, false to exit.
     */
    boolean execute(String args, ConsoleReader console);

    /** Gets the name used to call the command. */
    String getName();

    /** Gets a description for the 'help' command. */
    String getDescription();
}



