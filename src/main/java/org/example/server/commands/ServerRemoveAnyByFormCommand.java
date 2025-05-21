package org.example.server.commands;

import org.example.common.models.FormOfEducation;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;

import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Command 'RemoveAnyByFormCommand'
 * Removes by any Form of Education
 */

public class ServerRemoveAnyByFormCommand extends Command {
    private final CollectionManager collectionManager;
    private final Logger logger;

    public ServerRemoveAnyByFormCommand(CollectionManager collectionManager, Logger logger) {
        super("remove_any_by_form_of_education", "Removes one element with the specified form of education");
        this.collectionManager = collectionManager;
        this.logger = logger;
    }

    @Override
    public Response execute(Request request) {
        logger.log(Level.INFO, "Executing 'remove_any_by_form_of_education' command.");

        Object argument = request.getObject();

        if (!(argument instanceof FormOfEducation)) {
            logger.log(Level.WARNING, "Invalid argument type for 'remove_any_by_form_of_education'. Expected FormOfEducation enum.");
            return new Response(StatusCode.ERROR, "Argument must be a FormOfEducation enum value.");
        }

        FormOfEducation form = (FormOfEducation) argument;

        try {
            boolean removed = collectionManager.removeAnyByFormOfEducation(form);
            if (removed) {
                return new Response(StatusCode.OK, "Removed one element with form " + form);
            } else {
                return new Response(StatusCode.OK, "No element found with form " + form + ". Nothing removed.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'remove_any_by_form_of_education': " + e.getMessage(), e);
            return new Response(StatusCode.ERROR_SERVER, "Internal server error during remove_any_by_form_of_education.");
        }
    }
}
