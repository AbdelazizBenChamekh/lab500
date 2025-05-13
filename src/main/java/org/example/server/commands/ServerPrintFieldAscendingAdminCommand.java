package org.example.server.commands;

import org.example.common.models.Person;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors; // For converting Person to String if needed

public class ServerPrintFieldAscendingAdminCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'print_field_ascending_group_admin'.");
        // No arguments expected
        if (request.getArgument() != null) {
            logger.log(Level.WARNING, "'print_field_ascending_group_admin' received unexpected arguments.");
        }
        try {
            List<Person> sortedAdmins = collectionManager.getSortedGroupAdmins();

            // Option 1: Convert to Strings before sending
            List<String> adminStrings = sortedAdmins.stream()
                    .map(Person::toString) // Use Person's toString
                    .collect(Collectors.toList());
            // Join into a single multi-line string for the response body
            String responseBody = "Group admins sorted by name:\n" + String.join("\n  ", adminStrings);
            return Response.success(responseBody);

            // Option 2: Send List<Person> directly (Requires Person and its fields like Location to be Serializable)
            // return Response.success("Group admins sorted by name:", sortedAdmins);
            // Client would need to know how to handle List<Person> in displayResponse

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'print_field_ascending_group_admin': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error retrieving sorted admins.");
        }
    }
}