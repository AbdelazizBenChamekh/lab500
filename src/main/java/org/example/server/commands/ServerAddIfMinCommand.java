// File: src/main/java/org/example/server/commands/ServerAddIfMinCommand.java
package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerAddIfMinCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'add_if_min' command.");
        Object argument = request.getArgument();

        if (!(argument instanceof StudyGroup)) {
            logger.log(Level.WARNING, "Invalid argument type for 'add_if_min'. Expected StudyGroup.");
            return Response.error(StatusCode.ERROR, "Argument must be a StudyGroup object for 'add_if_min'.");
        }
        StudyGroup groupToAdd = (StudyGroup) argument;

        try {
            boolean added = collectionManager.addIfMin(groupToAdd);
            if (added) {
                return Response.success("Element added successfully (collection was empty or element was smaller).");
            } else {
                // This isn't an error, just that the condition wasn't met
                return Response.success("Element not added (not smaller than current minimum or collection not empty).");
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Validation error during 'add_if_min': " + e.getMessage());
            return Response.error(StatusCode.ERROR, "Invalid data for candidate element: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'add_if_min': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error during add_if_min.");
        }
    }
}