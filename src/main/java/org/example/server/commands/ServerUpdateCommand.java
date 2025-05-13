// File: src/main/java/org/example/server/commands/ServerUpdateCommand.java
package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerUpdateCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'update' command.");
        Object argument = request.getArgument();

        // Validate the argument package structure
        if (!(argument instanceof Object[])) {
            logger.log(Level.WARNING, "Invalid argument structure for 'update'. Expected Object[].");
            return Response.error(StatusCode.ERROR, "Invalid argument package for update.");
        }
        Object[] updateArgs = (Object[]) argument;
        if (updateArgs.length != 2 || !(updateArgs[0] instanceof Integer) || !(updateArgs[1] instanceof StudyGroup)) {
            logger.log(Level.WARNING, "Incorrect argument types/count for 'update'. Expected [Integer ID, StudyGroup object].");
            return Response.error(StatusCode.ERROR, "Update requires an [Integer ID, StudyGroup object] argument package.");
        }

        int idToUpdate = (Integer) updateArgs[0];
        StudyGroup updateData = (StudyGroup) updateArgs[1];

        // Basic ID validation
        if (idToUpdate <= 0) {
            logger.log(Level.WARNING, "Invalid ID provided for 'update': " + idToUpdate);
            return Response.error(StatusCode.ERROR, "ID must be positive for update.");
        }

        try {
            // Attempt the update in CollectionManager
            boolean updated = collectionManager.updateElement(idToUpdate, updateData);
            if (updated) {
                return Response.success("Element ID " + idToUpdate + " updated successfully.");
            } else {
                // ID not found is a processing failure, return error status
                logger.log(Level.INFO, "'update': ID " + idToUpdate + " not found.");
                return Response.error(StatusCode.ERROR, "Element ID " + idToUpdate + " not found.");
            }
        } catch (IllegalArgumentException e) {
            // Validation error during update (e.g., bad data in updateData)
            logger.log(Level.WARNING, "Validation error during 'update' for ID " + idToUpdate + ": " + e.getMessage());
            return Response.error(StatusCode.ERROR, "Invalid data provided for update: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'update' for ID " + idToUpdate + ": " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error during update.");
        }
    }
}