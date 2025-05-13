package org.example.client.commands;

import org.example.common.network.Request;
import org.example.client.ConsoleReader;

/**
 * Interface for client-side commands.
 * Responsible for parsing arguments, gathering necessary data (interactively if needed),
 * and creating the corresponding Request object to be sent to the server.
 */
public interface ClientCommand {
    /**
     * Prepares the Request object for this command based on user arguments.
     * May interact with the user via ConsoleReader to get complex data.
     *
     * @param argumentString The string part of the arguments entered by the user (can be null).
     * @param console The ConsoleReader for interactive input if needed.
     * @return The Request object to send to the server, or null if preparation failed (e.g., invalid args, user cancelled input).
     */
    Request prepareRequest(String argumentString, ConsoleReader console);

    /** Gets the name used to invoke the command. */
    String getName();

    /** Gets a description for client-side help (optional). */
    String getDescription(); // Can provide basic usage syntax
}


