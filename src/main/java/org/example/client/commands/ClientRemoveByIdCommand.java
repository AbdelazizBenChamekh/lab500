package org.example.client.commands;

import org.example.common.network.Request;
import org.example.client.ConsoleReader;

/**
 * Client command to remove an element by ID. Parses ID argument.
 */
public class ClientRemoveByIdCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString == null) {
            console.printError("ID argument is required for remove_by_id.");
            return null;
        }
        try {
            Integer idToRemove = Integer.parseInt(argumentString.trim());
            if (idToRemove <= 0) throw new IllegalArgumentException("ID must be positive.");
            // Create request with command name and the ID object as the argument
            return new Request("remove_by_id", idToRemove); // Send ID as object
        } catch (NumberFormatException e) {
            console.printError("Invalid ID format: '" + argumentString + "'. Must be an integer.");
            return null;
        } catch (IllegalArgumentException e) {
            console.printError(e.getMessage());
            return null;
        }
    }
    @Override public String getName() { return "remove_by_id"; }
    @Override public String getDescription() { return "remove_by_id <id> : remove element by its id"; }
}