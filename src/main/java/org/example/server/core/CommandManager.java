package org.example.server.core;

import org.example.common.network.StatusCode;
import org.example.common.network.User;
import org.example.server.commands.Command;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.server.exceptions.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * Command manager.
 * Implements the Command programming pattern.
 */
public class CommandManager {
    /**
     * Stores commands as Name-Command pairs.
     */
    private final Map<String, Command> commands = new HashMap<>();
    /**
     * Stores the command history.
     */
    private final List<String> commandHistory = new ArrayList<>();
    private final DatabaseManager databaseManager;

    private static final Logger logger = Logger.getLogger(CommandManager.class.getName());

    public CommandManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Registers a single command.
     */
    public void addCommand(Command command) {
        this.commands.put(command.getName(), command);
        logger.info("Command added: " + command.getName());
    }

    /**
     * Registers multiple commands.
     */
    public void addCommand(Collection<Command> commands) {
        for (Command command : commands) {
            addCommand(command);
        }
    }

    public Collection<Command> getCommands() {
        return commands.values();
    }

    public void addToHistory(String userLogin, String commandName) {
        if (commandName == null || commandName.isBlank()) return;
        String entry = userLogin + ": " + commandName;
        this.commandHistory.add(entry);
        logger.info("Added to history: " + entry);
    }


    public List<String> getCommandHistory(User user) {
        return new ArrayList<>(commandHistory);
    }

    /**
     * Executes a command.
     * @param request - client request
     * @throws NoSuchCommand if the command does not exist
     * @throws IllegalArguments if command arguments are invalid
     * @throws CommandRuntimeError if command execution failed
     * @throws ExitObliged if the command triggers program exit
     */
    public Response execute(Request request) throws NoSuchCommand, IllegalArguments, CommandRuntimeError, ExitObliged {
        Command command = commands.get(request.getCommandName());
        if (command == null) {
            logger.severe("No such command: " + request.getCommandName());
            throw new NoSuchCommand();
        }

        try {
            return command.execute(request);
        } catch (InvalidForm e) {
            logger.warning("Invalid form received for command: " + command.getName());
            return new Response(StatusCode.ERROR, "Ошибка: введены некорректные данные формы. " + e.getMessage());
        }
}}


