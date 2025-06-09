package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.DatabaseManager;

import java.sql.SQLException;

/**
 * Команда 'register'
 * Регистрирует пользователя
 */
public class ServerRegisterCommand extends Command {
    DatabaseManager databaseManager;

    public ServerRegisterCommand(DatabaseManager databaseManager) {
        super("register", ": Зарагестрировать пользователя");
        this.databaseManager = databaseManager;
    }

    /**
     * Исполнить команду
     * @param request запрос клиента
     * @throws IllegalArguments неверные аргументы команды
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        System.out.println("Регистрация пользователя: " + request.getUser().name());
        try {
            databaseManager.addUser(request.getUser());
        } catch (SQLException e) {
            System.out.println("Ошибка регистрации пользователя: " + request.getUser().name());
            return new Response(StatusCode.LOGIN_FAILED, "Введен невалидный пароль!");
        }
        System.out.println("Пользователь зарегистрирован: " + request.getUser().name());
        return new Response(StatusCode.OK,"Вы успешно зарегистрированы!");
    }

}