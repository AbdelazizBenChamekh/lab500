package org.example.server.commands;

import org.example.common.models.Person;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Command to print group admins in ascending order by name.
 */
public class ServerPrintFieldAscendingAdminCommand extends Command {
    private final CollectionManager collectionManager;
    private final Logger logger;

    public ServerPrintFieldAscendingAdminCommand(CollectionManager collectionManager, Logger logger) {
        super("print_field_ascending_group_admin", ": вывести значения поля groupAdmin всех элементов в порядке возрастания (по имени)");
        this.collectionManager = collectionManager;
        this.logger = logger;
    }

    @Override
    public Response execute(Request request) {
        logger.log(Level.INFO, "Executing 'print_field_ascending_group_admin' command for user: " +
                (request.getUser() != null ? request.getUser() : "Unknown"));

        if ((request.getArgs() != null && !request.getArgs().trim().isEmpty()) || request.getObject() != null) {
            logger.log(Level.WARNING, "'print_field_ascending_group_admin' received unexpected arguments.");
            return new Response(StatusCode.WRONG_ARGUMENTS, "Команда 'print_field_ascending_group_admin' не принимает аргументов.");
        }

        try {
            List<Person> sortedAdmins = collectionManager.getSortedGroupAdmins();

            if (sortedAdmins.isEmpty()) {
                return new Response(StatusCode.OK, "В коллекции нет администраторов групп (коллекция может быть пуста).");
            }

            List<String> adminStrings = sortedAdmins.stream()
                    .map(Person::toString)
                    .collect(Collectors.toList());

            String responseBody = "Администраторы групп (отсортированы по имени):\n  " +
                    String.join("\n  ", adminStrings); // Indent each admin

            return new Response(StatusCode.OK, responseBody);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'print_field_ascending_group_admin': " + e.getMessage(), e);
            return new Response(StatusCode.ERROR_SERVER, "Внутренняя ошибка сервера при получении отсортированных администраторов.");
        }
    }
}