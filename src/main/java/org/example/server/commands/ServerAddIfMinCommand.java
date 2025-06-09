package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.common.models.StudyGroup;
import org.example.common.network.User;
import org.example.server.core.CollectionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerAddIfMinCommand extends Command {
    private final CollectionManager collectionManager;
    private static final Logger logger = Logger.getLogger(ServerAddIfMinCommand.class.getName());

    public ServerAddIfMinCommand(CollectionManager collectionManager) {
        super("add_if_min", "добавить элемент, если он меньше минимального");
        this.collectionManager = collectionManager;
    }

    @Override
    public Response execute(Request request) {
        StudyGroup groupCandidate = request.getObject();
        User authenticatedUser = request.getUser();

        if (groupCandidate == null) {
            return new Response(StatusCode.WRONG_ARGUMENTS, "Объект StudyGroup обязателен.");
        }

        if (authenticatedUser == null) {
            return new Response(StatusCode.ERROR_AUTHENTICATION, "Требуется аутентификация пользователя.");
        }

        try {
            boolean added = collectionManager.addIfMin(groupCandidate, authenticatedUser);

            if (added) {
                return new Response(StatusCode.OK, "Элемент добавлен, так как он меньше минимального.");
            } else {
                return new Response(StatusCode.OK, "Элемент не добавлен, так как он не меньше минимального.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка при выполнении add_if_min", e);
            return new Response(StatusCode.ERROR_SERVER, "Ошибка сервера при выполнении add_if_min.");
        }
    }
}

