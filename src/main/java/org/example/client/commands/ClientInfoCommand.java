package org.example.client.commands;

import org.example.common.network.Request;
import org.example.client.ConsoleReader;

/**
 * Client command to request server info.
 */
public class ClientInfoCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError("Info command takes no arguments.");
            return null;
        }
        return new Request("info"); // Simple request with just command name
    }
    @Override public String getName() { return "info"; }
    @Override public String getDescription() { return "info : get information about the server's collection"; }
}