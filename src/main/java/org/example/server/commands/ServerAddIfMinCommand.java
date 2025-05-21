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
 * 'addIfMinimum' command
 * Adds a new element to the collection
 */

public class ServerAddIfMinCommand extends Command {

    private final CollectionManager collectionManager;
    private final Logger logger;

    public ServerAddIfMinCommand(CollectionManager collectionManager, Logger logger) {
        super("add_if_min", "Adds a new element if it is less than the current minimum");
        this.collectionManager = collectionManager;
        this.logger = logger;
    }

    @Override
    public Response execute(Request request) throws CommandRuntimeError, ExitObliged, IllegalArguments {
        logger.log(Level.INFO, "Executing 'add_if_min' command.");

        StudyGroup groupToAdd = request.getObject();
        if (groupToAdd == null) {
            logger.log(Level.WARNING, "No StudyGroup object provided in request.");
            throw new IllegalArguments("StudyGroup object is required for 'add_if_min'.");
        }

        try {
            boolean added = collectionManager.addIfMin(groupToAdd);
            if (added) {
                logger.log(Level.INFO, "Element added successfully.");
                return new Response(StatusCode.OK, "Element added successfully (collection was empty or element was smaller).");
            } else {
                logger.log(Level.INFO, "Element not added (not smaller than current minimum).");
                return new Response(StatusCode.OK, "Element not added (not smaller than current minimum).");
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Validation error: " + e.getMessage());
            throw new CommandRuntimeError("Invalid data for candidate element: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'add_if_min': " + e.getMessage(), e);
            throw new CommandRuntimeError("Internal server error during add_if_min.");
        }
    }
}
