package org.example.server.commands;

import org.example.common.network.*;
import org.example.server.exceptions.IllegalArguments;

/**
 * Команда 'ping'
 * пингануть сервер
 */
public class Ping extends Command {
    public Ping() {
        super("ping", ": пингануть сервер");
    }

    /**
     * Исполнить команду
     * @param request запрос клиента
     * @throws IllegalArguments неверные аргументы команды
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        return new Response(StatusCode.OK, "pong");
    }
}
