// File: src/main/java/org/example/server/commands/ServerClearCommand.java
package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerClearCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'clear' command.");
        // No arguments expected for clear
        if (request.getArgument() != null) {
            logger.log(Level.WARNING, "'clear' command received with unexpected argument.");
            // Optionally ignore argument or return error. Let's ignore for now.
        }
        try {
            collectionManager.clearCollection();
            return Response.success("Collection cleared successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'clear': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error while clearing collection.");
        }
    }
}