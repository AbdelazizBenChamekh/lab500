package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRemoveByIdCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'remove_by_id'.");
        Object argument = request.getArgument();

        if (!(argument instanceof Integer)) {
            logger.log(Level.WARNING, "Invalid argument type for 'remove_by_id'. Expected Integer ID.");
            return Response.error(StatusCode.ERROR, "Argument must be an Integer ID for remove_by_id.");
        }
        int idToRemove = (Integer) argument;

        if (idToRemove <= 0) {
            logger.log(Level.WARNING, "Invalid ID for 'remove_by_id': " + idToRemove);
            return Response.error(StatusCode.ERROR, "ID must be positive for remove_by_id.");
        }

        try {
            if (collectionManager.removeById(idToRemove)) {
                return Response.success("Element ID " + idToRemove + " removed.");
            } else {
                // ID not found is not necessarily a server *error*, but the operation failed.
                logger.log(Level.INFO, "'remove_by_id': ID " + idToRemove + " not found.");
                return Response.error(StatusCode.ERROR, "Element ID " + idToRemove + " not found."); // Use ERROR status
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'remove_by_id': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error removing element.");
        }
    }
}
