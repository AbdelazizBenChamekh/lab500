// File: src/main/java/org/example/server/commands/ServerCommand.java
package org.example.server.commands; // New package for server commands

import org.example.common.network.Request; // Needs request details
import org.example.common.network.Response; // Returns a response
import org.example.server.core.CollectionManager; // Needs access to data

import java.util.logging.Logger; // Optional: For logging within command

/**
 * Interface for server-side commands. Defines the contract for executing
 * a command based on a client request and interacting with the CollectionManager.
 */
@FunctionalInterface // Indicates this is intended as a functional interface
public interface ServerCommand {

    /**
     * Executes the server-side logic for a specific command.
     *
     * @param request The original Request object from the client, containing arguments.
     * @param collectionManager The CollectionManager instance to manipulate data.
     * @param logger The Logger instance for logging command execution details (optional).
     * @return A Response object containing the result or error information.
     */
    Response execute(Request request, CollectionManager collectionManager, Logger logger);

    // We might not need getName/getDescription on the server command interface itself,
    // as dispatching is done via map lookup in RequestHandler. But they can be added if useful.
    // String getName();
    // String getDescription(); // Server doesn't usually send help text this way
}