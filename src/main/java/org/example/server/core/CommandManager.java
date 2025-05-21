package org.example.server.core;

import org.example.common.models.StudyGroup;
import org.example.server.commands.CollectionEditor;
import org.example.server.commands.Command;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.server.exceptions.CommandRuntimeError;
import org.example.server.exceptions.ExitObliged;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.exceptions.NoSuchCommand;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Command manager.
 * Implements the Command programming pattern.
 */
public class CommandManager {
    /**
     * Stores commands as Name-Command pairs.
     */
    private final HashMap<String, Command> commands = new HashMap<>();
    /**
     * Stores the command history.
     */
    private final List<String> commandHistory = new ArrayList<>();
    private final FileManager fileManager;
    private LinkedHashSet<StudyGroup> collection;

    private static final Logger commandManagerLogger = Logger.getLogger(CommandManager.class.getName());

    public CommandManager(FileManager fileManager) {
        this.fileManager = fileManager;
        this.collection = fileManager.loadCollection();
    }

    public void addCommand(Command command) {
        this.commands.put(command.getName(), command);
        commandManagerLogger.info("Command added: " + command.getName());
    }

    public void addCommand(Collection<Command> commands) {
        this.commands.putAll(commands.stream()
                .collect(Collectors.toMap(Command::getName, s -> s)));
        commandManagerLogger.info("Commands added: " + commands.stream().map(Command::getName).collect(Collectors.joining(", ")));
    }

    public Collection<Command> getCommands() {
        return commands.values();
    }

    public void addToHistory(String line) {
        if (line.isBlank()) return;
        this.commandHistory.add(line);
        commandManagerLogger.info("Added to history: " + line);
    }

    public List<String> getCommandHistory() {
        return commandHistory;
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
            commandManagerLogger.severe("No such command: " + request.getCommandName());
            throw new NoSuchCommand();
        }
        Response response = command.execute(request);
        commandManagerLogger.info("Executed command: " + command.getName() + ", Response: " + response);
        if (command instanceof CollectionEditor) {
            commandManagerLogger.info("File updated after executing a collection editor command.");
            fileManager.saveCollection(collection);
        }
        return response;
    }
}

