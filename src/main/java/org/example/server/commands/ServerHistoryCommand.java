package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CommandManager;
import org.example.server.exceptions.IllegalArguments;

import java.util.List;

/**
 * Команда 'history'
 * Выводит последние 5 команд (без их аргументов)
 */
public class ServerHistoryCommand extends Command{
    private final CommandManager commandManager;
    public ServerHistoryCommand(CommandManager commandManager) {
        super("history", " вывести последние 5 команд (без их аргументов)");
        this.commandManager = commandManager;
    }

    /**
     * Исполнить команду
     * @param request аргументы команды
     * @throws IllegalArguments неверные аргументы команды
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (!request.getArgs().isBlank()) throw new IllegalArguments();
        List<String> history= commandManager.getCommandHistory(request.getUser());
        return new Response(StatusCode.OK,
                String.join("\n",
                        history.subList(Math.max(history.size() - 5, 0), history.size())));
    }
}