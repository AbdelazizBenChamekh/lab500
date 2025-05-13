package org.example.client.commands;

import org.example.common.network.Request;
import org.example.client.ConsoleReader;

/**
 * Client command to request group admins sorted by name from the server.
 */
public class ClientPrintFieldAscendingAdminCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError("print_field_ascending_group_admin command takes no arguments.");
            return null;
        }
        return new Request("print_field_ascending_group_admin");
    }

    @Override public String getName() { return "print_field_ascending_group_admin"; }
    @Override public String getDescription() { return "print_field_ascending_group_admin : request group admins sorted by name"; }
}