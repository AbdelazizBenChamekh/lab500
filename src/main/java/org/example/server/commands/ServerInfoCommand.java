// File: src/main/java/org/example/server/commands/ServerInfoCommand.java
package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerInfoCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'info' command.");
        try {
            String info = collectionManager.getInfo();
            return Response.success(info); // Use factory method
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'info': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error getting info.");
        }
    }
}