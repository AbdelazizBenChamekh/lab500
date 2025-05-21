package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;

/**
 * Command 'execute'
 * execute script
 */
public class ServerExecuteScript extends Command {

    public ServerExecuteScript() {
        super("execute_script", ": выполнить скрипт");
    }

    /**
     * Execute the command
     * @param request client request
     * @throws IllegalArguments invalid command arguments
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (request.getArgs().isBlank()) throw new IllegalArguments();
        return new Response(StatusCode.EXECUTE_SCRIPT, request.getArgs());
    }
}
