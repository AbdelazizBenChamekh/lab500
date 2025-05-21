package org.example.server.commands;

import org.example.common.network.*;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CommandManager;

/**
 * Command 'help'
 * display help on available commands
 */
public class ServerHelpCommand extends Command {
    private CommandManager commandManager;
    public ServerHelpCommand(CommandManager commandManager) {
        super("help", ": вывести справку по доступным командам");
        this.commandManager = commandManager;
    }

    /**
     * Execute the command
     * @param request client request
     * @throws IllegalArguments invalid command arguments
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (!request.getArgs().isBlank()) throw new IllegalArguments();
        return new Response(StatusCode.OK,
                String.join("\n",
                        commandManager.getCommands()
                                .stream().map(Command::toString).toList()));
    }
}
