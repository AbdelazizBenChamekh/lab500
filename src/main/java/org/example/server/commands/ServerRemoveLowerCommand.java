package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRemoveLowerCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'remove_lower' command.");
        Object argument = request.getArgument();

        if (!(argument instanceof StudyGroup)) {
            logger.log(Level.WARNING, "Invalid argument type for 'remove_lower'. Expected StudyGroup.");
            return Response.error(StatusCode.ERROR, "Argument must be a StudyGroup object for 'remove_lower'.");
        }
        StudyGroup thresholdGroup = (StudyGroup) argument;

        try {
            long removedCount = collectionManager.removeLower(thresholdGroup);
            return Response.success("Removed " + removedCount + " elements smaller than the provided threshold.");
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Validation error during 'remove_lower': " + e.getMessage());
            return Response.error(StatusCode.ERROR, "Invalid data for threshold element: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'remove_lower': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error during remove_lower.");
        }
    }
}