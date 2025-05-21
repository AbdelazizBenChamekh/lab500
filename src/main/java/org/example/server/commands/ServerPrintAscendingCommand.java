package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerPrintAscendingCommand extends Command {
    private final CollectionManager collectionManager;
    private final Logger logger;

    public ServerPrintAscendingCommand(CollectionManager collectionManager, Logger logger) {
        super("print_ascending", "Prints all elements in ascending order by ID");
        this.collectionManager = collectionManager;
        this.logger = logger;
    }

    @Override
    public Response execute(Request request) {
        logger.log(Level.INFO, "Executing 'print_ascending' command.");

        if (request.getObject() != null) {
            logger.log(Level.WARNING, "'print_ascending' received unexpected argument.");
            return new Response(StatusCode.ERROR, "'print_ascending' command does not accept arguments.");
        }

        try {
            List<StudyGroup> ascendingGroups = collectionManager.getAllGroupsSortedById();
            return new Response(StatusCode.OK, "Collection elements sorted by ID:", ascendingGroups);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'print_ascending': " + e.getMessage(), e);
            return new Response(StatusCode.ERROR_SERVER, "Internal server error retrieving sorted elements.");
        }
    }
}
