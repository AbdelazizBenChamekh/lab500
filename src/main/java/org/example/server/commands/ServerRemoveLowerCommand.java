package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import org.example.server.exceptions.CommandRuntimeError;
import org.example.server.exceptions.ExitObliged;
import org.example.server.exceptions.IllegalArguments;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * 'RemoveLower' command
 * Removes lower part.
 */

public class ServerRemoveLowerCommand extends Command {

    private final CollectionManager collectionManager;
    private final Logger logger;

    public ServerRemoveLowerCommand(CollectionManager collectionManager, Logger logger) {
        super("remove_lower", "Removes all elements smaller than the provided one");
        this.collectionManager = collectionManager;
        this.logger = logger;
    }

    @Override
    public Response execute(Request request) throws CommandRuntimeError, ExitObliged, IllegalArguments {
        logger.log(Level.INFO, "Executing 'remove_lower' command.");

        StudyGroup thresholdGroup = request.getObject();
        if (thresholdGroup == null) {
            logger.log(Level.WARNING, "No StudyGroup object provided in request.");
            throw new IllegalArguments("StudyGroup object is required for 'remove_lower'.");
        }

        try {
            long removedCount = collectionManager.removeLower(thresholdGroup);
            logger.log(Level.INFO, "Removed " + removedCount + " elements smaller than the threshold.");
            return new Response(StatusCode.OK, "Removed " + removedCount + " elements smaller than the provided threshold.");
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Validation error during 'remove_lower': " + e.getMessage());
            throw new CommandRuntimeError("Invalid data for threshold element: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'remove_lower': " + e.getMessage(), e);
            throw new CommandRuntimeError("Internal server error during remove_lower.");
        }
    }
}
