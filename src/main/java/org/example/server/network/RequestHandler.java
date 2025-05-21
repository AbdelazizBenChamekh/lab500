package org.example.server.network;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.CommandRuntimeError;
import org.example.server.exceptions.ExitObliged;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.exceptions.NoSuchCommand;
import org.example.server.core.CommandManager;

public class RequestHandler {
    private CommandManager commandManager;

    public RequestHandler(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public Response handle(Request request) {
        try {
            commandManager.addToHistory(request.getCommandName());
            return commandManager.execute(request);
        } catch (IllegalArguments e) {
            return new Response(StatusCode.WRONG_ARGUMENTS,
                    "Неверное использование аргументов команды");
        } catch (CommandRuntimeError e) {
            return new Response(StatusCode.ERROR,
                    "Ошибка при исполнении программы");
        } catch (NoSuchCommand e) {
            return new Response(StatusCode.ERROR, "Такой команды нет в списке");
        } catch (ExitObliged e) {
            return new Response(StatusCode.EXIT);
        }
    }
}