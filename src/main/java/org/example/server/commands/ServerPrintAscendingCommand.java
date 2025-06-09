package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerPrintAscendingCommand extends Command {
    private final CollectionManager collectionManager;
    private final Logger logger; // Good to keep for consistent logging

    public ServerPrintAscendingCommand(CollectionManager collectionManager, Logger logger) {
        super("print_ascending", ": вывести элементы коллекции в порядке возрастания ID");
        this.collectionManager = collectionManager;
        this.logger = logger; // Assign the passed logger
    }

    /**
     * Executes the 'print_ascending' command.
     * Retrieves all study groups sorted by their natural order (ID) from the CollectionManager.
     * This command requires authentication (User object in Request).
     *
     * @param request The client request. Expected to have a User object, but no string or StudyGroup arguments.
     * @return A Response object containing the sorted list of StudyGroups or an error.
     */
    @Override
    public Response execute(Request request) /* throws IllegalArguments - Removed, handle with Response */ {
        // Log the attempt, including user if available (authentication is handled by RequestHandler before this)
        String userLogin = (request.getUser() != null && request.getUser() != null)
                ? String.valueOf(request.getUser()) : "Unauthenticated/Unknown";
        logger.log(Level.INFO, "Executing 'print_ascending' command for user: " + userLogin);

        // Check for unexpected arguments using the new getter names from the refactored Request class
        boolean hasUnexpectedStringArg = request.getArgs() != null && !request.getArgs().trim().isEmpty();
        boolean hasUnexpectedStudyGroupObject = request.getObject() != null;

        if (hasUnexpectedStringArg || hasUnexpectedStudyGroupObject) {
            logger.log(Level.WARNING, "'print_ascending' command received unexpected arguments. StringArg: '" +
                    request.getArgs() + "', StudyGroupObject: " + (request.getObject() != null));
            return new Response(StatusCode.WRONG_ARGUMENTS, "Команда 'print_ascending' не принимает аргументов.");
        }

        try {
            // CollectionManager is responsible for providing the data (already sorted by ID by default)
            // Ensure getAllGroupsSortedById() exists and functions correctly in CollectionManager.
            List<StudyGroup> ascendingGroups = collectionManager.getAllGroupsSortedById();

            if (ascendingGroups == null) { // Should ideally not happen if CM is initialized
                logger.log(Level.SEVERE, "'getAllGroupsSortedById' returned null.");
                return new Response(StatusCode.ERROR_SERVER, "Ошибка сервера: не удалось получить список элементов.");
            }

            logger.log(Level.INFO, "Successfully retrieved " + ascendingGroups.size() + " groups for 'print_ascending'.");
            // The message can be generic, or the client can format it.
            // For consistency, let's provide a basic message and the collection data.
            return new Response(StatusCode.OK, "Элементы коллекции (отсортированные по ID):", ascendingGroups);

        } catch (Exception e) {
            // Catch any unexpected runtime exceptions from CollectionManager or other operations
            logger.log(Level.SEVERE, "Error executing 'print_ascending' for user " + userLogin + ": " + e.getMessage(), e);
            return new Response(StatusCode.ERROR_SERVER, "Внутренняя ошибка сервера при получении отсортированных элементов.");
        }
    }
}