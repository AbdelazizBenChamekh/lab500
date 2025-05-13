package org.example.client.commands;

import org.example.common.network.Request;
import org.example.client.ConsoleReader;
import java.util.Map; // Need map to display help

/**
 * Client-side help command. Can display client commands or send 'help' request to server.
 */
public class ClientHelpCommand implements ClientCommand {
    // Store reference to the map for displaying client-side help
    private final Map<String, ClientCommand> clientCommandMap;

    public ClientHelpCommand(Map<String, ClientCommand> clientCommandMap) {
        this.clientCommandMap = clientCommandMap;
    }

    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError("Help command takes no arguments.");
            return null; // Indicate failure to prepare request
        }
        // Option 1: Display client-side descriptions
        console.println("--- Client Available Commands ---");
        for (ClientCommand cmd : clientCommandMap.values()) {
            console.println(cmd.getDescription());
        }
        console.println("-----------------------------");
        // Option 2: Send 'help' request to server (uncomment if preferred)
        // return new Request("help");

        // Since help is primarily client-side info here, we don't send a request
        return null; // Return null to signify no server request needed
    }

    @Override public String getName() { return "help"; }
    @Override public String getDescription() { return "help : display available client commands"; }
}
