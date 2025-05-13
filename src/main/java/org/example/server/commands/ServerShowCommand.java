package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerShowCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'show' command.");
        try {
            List<StudyGroup> allGroups = collectionManager.getAllGroupsSortedBySize();
            return Response.success("Current collection elements:", allGroups);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'show': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error getting collection.");
        }
    }
}