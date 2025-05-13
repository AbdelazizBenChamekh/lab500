// File: src/main/java/org/example/client/commands/ClientPrintAscendingCommand.java
package org.example.client.commands;

import org.example.common.network.Request;
import org.example.client.ConsoleReader;

/**
 * Client command to request elements sorted by ID from the server.
 */
public class ClientPrintAscendingCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError("print_ascending command takes no arguments.");
            return null;
        }
        return new Request("print_ascending");
    }

    @Override public String getName() { return "print_ascending"; }
    @Override public String getDescription() { return "print_ascending : request elements sorted by ID"; }
}