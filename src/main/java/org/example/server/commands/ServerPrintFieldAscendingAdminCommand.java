package org.example.server.commands;

import org.example.common.models.Person;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Command to print group admins in ascending order.
 */
public class ServerPrintFieldAscendingAdminCommand extends Command {
    private final CollectionManager collectionManager;
    private final Logger logger;

    public ServerPrintFieldAscendingAdminCommand(CollectionManager collectionManager, Logger logger) {
        super("print_field_ascending_group_admin", "Prints group admins in ascending order");
        this.collectionManager = collectionManager;
        this.logger = logger;
    }

    @Override
    public Response execute(Request request) {
        logger.log(Level.INFO, "Executing 'print_field_ascending_group_admin' command.");

        Object argument = request.getObject();
        if (argument != null) {
            logger.log(Level.WARNING, "'print_field_ascending_group_admin' received unexpected arguments.");
            return new Response(StatusCode.ERROR, "'print_field_ascending_group_admin' does not accept arguments.");
        }

        try {
            List<Person> sortedAdmins = collectionManager.getSortedGroupAdmins();

            if (sortedAdmins.isEmpty()) {
                return new Response(StatusCode.OK, "No group admins found in the collection.");
            }

            List<String> adminStrings = sortedAdmins.stream()
                    .map(Person::toString)
                    .collect(Collectors.toList());
            String responseBody = "Group admins sorted by name:\n  " + String.join("\n  ", adminStrings);

            return new Response(StatusCode.OK, responseBody);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'print_field_ascending_group_admin': " + e.getMessage(), e);
            return new Response(StatusCode.ERROR_SERVER, "Internal server error retrieving sorted admins.");
        }
    }
}
