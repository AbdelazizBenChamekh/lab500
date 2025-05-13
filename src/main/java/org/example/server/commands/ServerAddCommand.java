package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerAddCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'add' command.");
        Object argument = request.getArgument();

        if (!(argument instanceof StudyGroup)) {
            logger.log(Level.WARNING, "Invalid argument type for 'add'. Expected StudyGroup.");
            return Response.error(StatusCode.ERROR, "Argument must be a StudyGroup object for 'add'.");
        }
        StudyGroup groupToAdd = (StudyGroup) argument;

        try {
            collectionManager.addElement(groupToAdd); // Handles ID/Date assignment
            return Response.success("Element added successfully.");
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Validation error during 'add': " + e.getMessage());
            return Response.error(StatusCode.ERROR, "Invalid data for new element: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'add': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error adding element.");
        }
    }
}