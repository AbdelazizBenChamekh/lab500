package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CommandManager;

import java.util.List;


/**
 * Command 'history'
 * Prints the last 5 commands (without their arguments)
 */
public class ServerHistoryCommand extends Command{
    private CommandManager commandManager;
    public ServerHistoryCommand(CommandManager commandManager) {
        super("history", " вывести последние 5 команд (без их аргументов)");
        this.commandManager = commandManager;
    }

    /**
     * Execute command
     * @param request command arguments
     * @throws IllegalArguments invalid command arguments
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (!request.getArgs().isBlank()) throw new IllegalArguments();
        List<String> history= commandManager.getCommandHistory();
        return new Response(StatusCode.OK,
                String.join("\n",
                        history.subList(Math.max(history.size() - 5, 0), history.size())));
    }
}