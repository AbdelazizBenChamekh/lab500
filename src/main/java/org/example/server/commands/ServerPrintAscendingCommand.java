// File: src/main/java/org/example/server/commands/ServerPrintAscendingCommand.java
package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerPrintAscendingCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'print_ascending'.");
        // No arguments expected
        if (request.getArgument() != null) {
            logger.log(Level.WARNING, "'print_ascending' received unexpected arguments.");
        }
        try {
            List<StudyGroup> ascendingGroups = collectionManager.getAllGroupsSortedById();
            // Send the list back with a success status
            return Response.success("Collection elements sorted by ID:", ascendingGroups);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'print_ascending': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error retrieving sorted elements.");
        }
    }
}