package org.example.client.commands;

import org.example.common.network.Request;
import org.example.client.ConsoleReader;

/**
 * Client command to request clearing the collection on the server.
 */
public class ClientClearCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError("Clear command takes no arguments.");
            return null;
        }
        return new Request("clear");
    }
    @Override public String getName() { return "clear"; }
    @Override public String getDescription() { return "clear : clear the collection on the server"; }
}