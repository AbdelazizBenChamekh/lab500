package org.example.client.commands;

import org.example.common.network.Request;
import org.example.client.ConsoleReader;

/**
 * Client command to request all elements from the server (server will sort by size).
 */
public class ClientShowCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError("Show command takes no arguments.");
            return null;
        }
        return new Request("show"); // Request just the command name
    }
    @Override public String getName() { return "show"; }
    @Override public String getDescription() { return "show : get all collection elements from server (sorted by size)"; }
}