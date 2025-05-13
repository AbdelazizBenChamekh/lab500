package org.example.server.network;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import org.example.server.commands.*;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler {
    private final CollectionManager collectionManager;
    private final Logger logger;
    private final Map<String, ServerCommand> commandMap;

    public RequestHandler(CollectionManager collectionManager, Logger logger) {
        this.collectionManager = collectionManager;
        this.logger = logger;
        this.commandMap = registerServerCommands();
        logger.info("RequestHandler initialized with " + commandMap.size() + " server commands.");
    }

    private Map<String, ServerCommand> registerServerCommands() {
        Map<String, ServerCommand> commands = new HashMap<>();
        commands.put("info", new ServerInfoCommand());
        commands.put("show", new ServerShowCommand());
        commands.put("add", new ServerAddCommand());
        commands.put("update", new ServerUpdateCommand());
        commands.put("remove_by_id", new ServerRemoveByIdCommand());
        commands.put("clear", new ServerClearCommand());
        commands.put("add_if_min", new ServerAddIfMinCommand());
        commands.put("remove_lower", new ServerRemoveLowerCommand());
        commands.put("remove_any_by_form_of_education", new ServerRemoveAnyByFormCommand());
        commands.put("print_ascending", new ServerPrintAscendingCommand());
        commands.put("print_field_ascending_group_admin", new ServerPrintFieldAscendingAdminCommand());

        return commands;
    }

    public Response handle(Request request) {
        if (request == null || request.getCommandName() == null || request.getCommandName().trim().isEmpty()) {
            logger.warning("Received empty or invalid request.");
            return Response.error(StatusCode.ERROR, "Empty or invalid request received by server.");
        }

        String commandName = request.getCommandName().toLowerCase().trim();
        logger.log(Level.INFO, "Handling request for command: '" + commandName + "'");

        if (commandName.equals("exit") || commandName.equals("history") || commandName.equals("execute_script") || commandName.equals("save") || commandName.equals("help")) {
            logger.log(Level.WARNING, "Client attempted to execute restricted/client-only command: '" + commandName + "'");
            return Response.error(StatusCode.ERROR, "Command '" + commandName + "' cannot be executed by client or is handled differently.");
        }

        ServerCommand command = commandMap.get(commandName);

        if (command != null) {
            logger.log(Level.FINE, "Executing command '" + commandName + "' using handler: " + command.getClass().getSimpleName());
            try {
                // Assuming ServerCommand.execute takes Request, CollectionManager, Logger
                return command.execute(request, collectionManager, logger);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected internal server error executing command '" + commandName + "'.", e);
                return Response.error(StatusCode.ERROR_SERVER, "Internal server error processing command '" + commandName + "'. Details: " + e.getMessage());
            }
        } else {
            logger.log(Level.WARNING, "Received unknown command: '" + commandName + "'");
            return Response.error(StatusCode.ERROR, "Unknown command received by server: " + commandName);
        }
    }
}