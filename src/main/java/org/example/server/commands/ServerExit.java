package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.ExitObliged;
import org.example.server.exceptions.IllegalArguments;

/**
 * Command 'exit'
 * terminate the program (without saving to file)
 */
public class ServerExit extends Command  {
    public ServerExit(){
        super("exit", ": завершить программу (без сохранения в файл)");
    }

    /**
     * Execute command
     * @param request command arguments
     * @throws ExitObliged program exit is needed
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (!request.getArgs().isBlank()) throw new IllegalArguments();
        return new Response(StatusCode.EXIT);
    }
}