// File: src/main/java/org/example/server/commands/ServerRemoveAnyByFormCommand.java
package org.example.server.commands;

import org.example.common.models.FormOfEducation;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRemoveAnyByFormCommand implements ServerCommand {
    @Override
    public Response execute(Request request, CollectionManager collectionManager, Logger logger) {
        logger.log(Level.FINE, "Executing 'remove_any_by_form_of_education'.");
        Object argument = request.getArgument();

        if (!(argument instanceof FormOfEducation)) {
            logger.log(Level.WARNING, "Invalid argument type for 'remove_any_by_form_of_education'. Expected FormOfEducation enum.");
            return Response.error(StatusCode.ERROR, "Argument must be a FormOfEducation enum value.");
        }
        FormOfEducation form = (FormOfEducation) argument;

        try {
            boolean removed = collectionManager.removeAnyByFormOfEducation(form);
            if (removed) {
                return Response.success("Removed one element with form " + form);
            } else {
                return Response.success("No element found with form " + form + ". Nothing removed."); // Still OK status
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing 'remove_any_by_form_of_education': " + e.getMessage(), e);
            return Response.error(StatusCode.ERROR_SERVER, "Internal server error during remove_any_by_form_of_education.");
        }
    }
}