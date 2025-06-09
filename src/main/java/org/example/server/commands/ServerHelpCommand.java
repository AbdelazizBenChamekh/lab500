package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CommandManager;
import org.example.server.exceptions.IllegalArguments;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * 'help' command for the server.
 * Provides a list of available server-side commands using their toString() representation.
 */
public class ServerHelpCommand extends Command { // Extends your abstract Command
    private final CommandManager commandManager;
    private static final Logger logger = Logger.getLogger(ServerHelpCommand.class.getName());

    public ServerHelpCommand(CommandManager commandManager) {
        super("help", ": вывести справку по доступным командам сервера");
        this.commandManager = commandManager;
    }

    /**
     * Executes the command to provide help information.
     * @param request client request (arguments are ignored for basic help).
     * @return Response object containing the help text.
     * @throws IllegalArguments if unexpected arguments are provided.
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (request.getArgs() != null && !request.getArgs().trim().isEmpty()) {
            logger.warning("'help' command received unexpected arguments: " + request.getArgs());
            throw new IllegalArguments("Команда 'help' в данный момент не принимает аргументов.");
        }

        logger.info("Executing 'help' command for user: " + (request.getUser() != null ? request.getUser() : "Unknown/Unauthenticated"));

        String helpText = commandManager.getCommands().stream()
                .sorted(Comparator.comparing(Command::getName))
                .map(Command::toString)
                .collect(Collectors.joining("\n"));

        return new Response(StatusCode.OK, "Доступные команды сервера:\n" + helpText);
    }
}