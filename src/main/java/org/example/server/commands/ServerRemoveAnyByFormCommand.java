package org.example.server.commands;

import org.example.common.models.FormOfEducation; // Keep this
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.common.network.User;
import org.example.server.core.CollectionManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 'remove_any_by_form_of_education' command for the server.
 * Removes one element owned by the authenticated user that matches the specified form of education.
 */
public class ServerRemoveAnyByFormCommand extends Command {
    private final CollectionManager collectionManager;
    private final Logger logger;

    public ServerRemoveAnyByFormCommand(CollectionManager collectionManager, Logger logger) {
        super("remove_any_by_form_of_education", "<FormOfEducation>: удалить из коллекции один элемент, значение поля formOfEducation которого эквивалентно заданному (принадлежащий вам)");
        this.collectionManager = collectionManager;
        this.logger = logger;
    }

    /**
     * Executes the command to remove an element by its form of education, checking ownership.
     * @param request client request, containing User info and FormOfEducation as the object argument.
     * @return Response object indicating success or failure.
     */
    @Override
    public Response execute(Request request) /* throws IllegalArguments - if you re-add */ {
        User authenticatedUser = request.getUser();
        Object argumentObject = request.getObject(); // The FormOfEducation enum is expected here

        // Authentication Check
        if (authenticatedUser == null) {
            logger.severe("User not authenticated for 'remove_any_by_form_of_education'. This should be caught earlier.");
            return new Response(StatusCode.ERROR_AUTHENTICATION, "User not authenticated.");
        }

        logger.log(Level.INFO, "User '" + authenticatedUser.getLogin() +
                "' executing 'remove_any_by_form_of_education' command.");

        // Validate Argument Type
        if (!(argumentObject instanceof FormOfEducation)) {
            logger.log(Level.WARNING, "Invalid argument type for 'remove_any_by_form_of_education'. " +
                    "Expected FormOfEducation enum, got: " +
                    (argumentObject != null ? argumentObject.getClass().getName() : "null"));
            return new Response(StatusCode.WRONG_ARGUMENTS, "Аргумент должен быть значением перечисления FormOfEducation.");
        }

        //  Check for unexpected string arguments
        if (request.getArgs() != null && !request.getArgs().trim().isEmpty()) {
            logger.log(Level.WARNING, "'remove_any_by_form_of_education' received unexpected string arguments: " + request.getArgs());
            return new Response(StatusCode.WRONG_ARGUMENTS, "Команда 'remove_any_by_form_of_education' не принимает строковых аргументов, только объект FormOfEducation.");
        }


        FormOfEducation formToMatch = (FormOfEducation) argumentObject;
        logger.info("Attempting to remove an element with FormOfEducation: " + formToMatch + " for user: " + authenticatedUser.getLogin());

        try {
            // 4. Delegate to CollectionManager, passing the authenticated user for ownership check
            boolean removed = collectionManager.removeAnyByFormOfEducation(formToMatch, authenticatedUser);

            if (removed) {
                logger.log(Level.INFO, "Successfully removed one element with form " + formToMatch + " owned by " + authenticatedUser.getLogin());
                return new Response(StatusCode.OK, "Один элемент с формой обучения '" + formToMatch + "', принадлежащий вам, был удален.");
            } else {
                logger.log(Level.INFO, "No element with form " + formToMatch + " owned by user " + authenticatedUser.getLogin() +
                        " found, or deletion failed. Nothing removed by this command.");
                return new Response(StatusCode.OK, "Элемент с формой обучения '" + formToMatch + "', принадлежащий вам, не найден. Ничего не удалено.");
            }
        } catch (Exception e) { // Catch unexpected errors from CollectionManager
            logger.log(Level.SEVERE, "Error executing 'remove_any_by_form_of_education' for user " + authenticatedUser.getLogin() +
                    " and form " + formToMatch + ": " + e.getMessage(), e);
            return new Response(StatusCode.ERROR_SERVER, "Внутренняя ошибка сервера при выполнении команды remove_any_by_form_of_education.");
        }
    }
}
