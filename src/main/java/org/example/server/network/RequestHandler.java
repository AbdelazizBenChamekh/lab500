// File: src/main/java/org/example/server/network/RequestHandler.java
package org.example.server.network; // Assuming this package

// Common models and network classes
import org.example.common.models.*;
import org.example.common.network.Request;
import org.example.common.network.Response; // Assuming Response has statusCode, responseBody, errorMessage
import org.example.common.network.StatusCode;
// Server core classes
import org.example.server.core.CollectionManager;


import java.util.List;
import java.util.stream.Collectors;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processes validated Request objects, interacts with CollectionManager,
 * and generates Response objects. Uses Stream API for collection operations.
 * Includes logging of request handling steps.
 */
public class RequestHandler {
    private final CollectionManager collectionManager;
    private final Logger logger; // <<< Logger instance

    /**
     * Constructor.
     * @param collectionManager The manager for collection operations.
     * @param parentLogger The logger from the parent component (e.g., ServerApp).
     */
    public RequestHandler(CollectionManager collectionManager, Logger parentLogger) {
        this.collectionManager = collectionManager;
        // Get a logger named after this class
        this.logger = Logger.getLogger(RequestHandler.class.getName());
        // Optional: Set parent to inherit handlers/levels if desired
        // this.logger.setParent(parentLogger);
        logger.config("RequestHandler initialized."); // Log initialization
    }

    /**
     * Handles a deserialized Request and returns a Response. Includes logging.
     * @param request The request from the client. Assumed not null here.
     * @return The response to send back to the client.
     */
    public Response handle(Request request) {
        // Assume request is not null based on ServerApp check
        String commandName = request.getCommandName().toLowerCase();
        Object argument = request.getCommandArgument(); // Using the assumed simpler Request structure
        String responseBody = null;
        String errorMessage = null;
        StatusCode statusCode = StatusCode.OK; // Assume success initially

        // Log start of handling
        logger.log(Level.INFO, "Handling command [{0}] with argument type: {1}",
                new Object[]{commandName, (argument == null ? "null" : argument.getClass().getSimpleName())});

        try {
            // Process commands using switch statement
            switch (commandName) {
                case "help":
                    logger.fine("Processing 'help' command.");
                    responseBody = getHelpText();
                    break;

                case "info":
                    logger.fine("Processing 'info' command.");
                    responseBody = collectionManager.getInfo();
                    break;

                case "show":
                    logger.fine("Processing 'show' command (sorting by student count).");
                    List<StudyGroup> sortedGroups = collectionManager.getElementsByStudentCount(); // Use your method name
                    if (sortedGroups.isEmpty()) {
                        responseBody = "Collection is empty.";
                    } else {
                        responseBody = sortedGroups.stream()
                                .map(StudyGroup::toString) // Use object's toString
                                .collect(Collectors.joining("\n"));
                        logger.fine("Prepared 'show' response with " + sortedGroups.size() + " elements.");
                    }
                    break;

                case "add":
                    logger.fine("Processing 'add' command.");
                    // Use helper to check type, throws IllegalArgumentException if wrong
                    ensureArgumentType(argument, StudyGroup.class, commandName);
                    collectionManager.addElement((StudyGroup) argument); // This should log success/failure internally now
                    responseBody = "Element added successfully.";
                    break;

                case "update":
                    logger.fine("Processing 'update' command.");
                    // Use helper to validate specific structure for update
                    validateUpdateArgs(argument);
                    Object[] updateArgs = (Object[]) argument;
                    int idToUpdate = (Integer) updateArgs[0];
                    StudyGroup updateData = (StudyGroup) updateArgs[1];
                    logger.fine("Attempting update for ID: " + idToUpdate);
                    if (!collectionManager.updateElement(idToUpdate, updateData)) {
                        // Log failure on server side
                        logger.warning("Update failed for ID " + idToUpdate + " (not found or internal error).");
                        errorMessage = "Element with ID " + idToUpdate + " not found or update failed.";
                        statusCode = StatusCode.ERROR_NOT_FOUND;
                    } else {
                        responseBody = "Element ID " + idToUpdate + " updated.";
                        logger.info("Successfully updated element ID " + idToUpdate);
                    }
                    break;

                case "remove_by_id":
                    logger.fine("Processing 'remove_by_id' command.");
                    ensureArgumentType(argument, Integer.class, commandName);
                    int idToRemove = (Integer) argument;
                    logger.fine("Attempting removal for ID: " + idToRemove);
                    if (!collectionManager.removeById(idToRemove)) {
                        logger.warning("Removal failed for ID " + idToRemove + " (not found).");
                        errorMessage = "Element with ID " + idToRemove + " not found.";
                        statusCode = StatusCode.ERROR_NOT_FOUND; // Or ERROR
                    } else {
                        responseBody = "Element with ID " + idToRemove + " removed.";
                        logger.info("Successfully removed element ID " + idToRemove);
                    }
                    break;

                case "clear":
                    logger.fine("Processing 'clear' command.");
                    collectionManager.clearCollection(); // Assumes this method logs if needed
                    responseBody = "Collection cleared.";
                    logger.info("Collection cleared by request.");
                    break;

                case "add_if_min":
                    logger.fine("Processing 'add_if_min' command.");
                    ensureArgumentType(argument, StudyGroup.class, commandName);
                    if (collectionManager.addIfMin((StudyGroup) argument)) {
                        responseBody = "Element added successfully (was smaller or collection was empty).";
                        logger.fine("'add_if_min' resulted in element addition.");
                    } else {
                        responseBody = "Element not added (not smaller than minimum).";
                        logger.fine("'add_if_min' did not add element.");
                    }
                    break;

                case "remove_lower":
                    logger.fine("Processing 'remove_lower' command.");
                    ensureArgumentType(argument, StudyGroup.class, commandName);
                    int removedCount = collectionManager.removeLower((StudyGroup) argument);
                    responseBody = "Removed " + removedCount + " elements smaller than the threshold.";
                    logger.info("Removed " + removedCount + " elements via 'remove_lower'.");
                    break;

                case "history": // Client-side only
                    logger.warning("Received client-side command 'history' on server.");
                    errorMessage = "Command 'history' is client-side only.";
                    statusCode = StatusCode.ERROR_CLIENT_COMMAND; // Use a specific status if defined
                    break;

                case "remove_any_by_form_of_education":
                    logger.fine("Processing 'remove_any_by_form_of_education' command.");
                    ensureArgumentType(argument, FormOfEducation.class, commandName);
                    FormOfEducation form = (FormOfEducation) argument;
                    logger.fine("Attempting removal for FormOfEducation: " + form);
                    if (collectionManager.removeAnyByFormOfEducation(form)) {
                        responseBody = "Removed one element with form " + form + ".";
                        logger.info("Removed one element via 'remove_any_by_form_of_education' for form " + form);
                    } else {
                        responseBody = "No element found with form " + form + ".";
                        logger.info("No element found for removal with form " + form);
                    }
                    break;

                case "print_ascending": // Sort by natural order (ID)
                    logger.fine("Processing 'print_ascending' command.");
                    List<StudyGroup> ascGroups = collectionManager.getSortedElements(); // Assumes method exists
                    if (ascGroups.isEmpty()) {
                        responseBody = "Collection is empty.";
                    } else {
                        responseBody = ascGroups.stream()
                                .map(StudyGroup::toString)
                                .collect(Collectors.joining("\n"));
                        logger.fine("Prepared 'print_ascending' response with " + ascGroups.size() + " elements.");
                    }
                    break;

                case "print_field_ascending_group_admin": // Sort admins by name
                    logger.fine("Processing 'print_field_ascending_group_admin' command.");
                    List<Person> sortedAdmins = collectionManager.getSortedGroupAdmins();
                    if (sortedAdmins.isEmpty()) {
                        responseBody = "No group admins found (collection might be empty).";
                    } else {
                        responseBody = sortedAdmins.stream()
                                .map(Person::toString)
                                .collect(Collectors.joining("\n"));
                        logger.fine("Prepared 'print_field_ascending_group_admin' response with " + sortedAdmins.size() + " admins.");
                    }
                    break;

                // Server-specific commands
                case "server_save":
                    logger.fine("Processing 'server_save' command.");
                    if (argument != null) {
                        // Use logger for warning about unexpected argument
                        logger.warning("'server_save' received an unexpected argument, ignoring it.");
                    }
                    collectionManager.saveCollection();
                    responseBody = "Collection saved successfully on server.";
                    logger.info("Collection saved via 'server_save' command.");
                    break;


                case "execute_script":
                    logger.warning("Received 'execute_script' command from client - this is not allowed.");
                    errorMessage = "Command 'execute_script' cannot be executed remotely.";
                    statusCode = StatusCode.ERROR_CLIENT_COMMAND;
                    break;

                case "exit":
                    logger.warning("Received 'exit' command from client - client should handle this.");
                    errorMessage = "Command 'exit' should be handled client-side.";
                    statusCode = StatusCode.ERROR_CLIENT_COMMAND;
                    break;


                default:
                    logger.warning("Unknown or unsupported command received: '" + commandName + "'");
                    errorMessage = "Unknown or unsupported command: '" + commandName + "'";
                    statusCode = StatusCode.ERROR_UNKNOWN_COMMAND; // Use specific status if defined
                    break;
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Argument/Validation Error processing command [" + commandName + "]", e);
            errorMessage = "Command Error [" + commandName + "]: " + e.getMessage();
            statusCode = StatusCode.ERROR_ARGUMENT;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Internal Server Error processing command [" + commandName + "]", e);
            errorMessage = "Internal Server Error processing [" + commandName + "]: " + e.getMessage();
            statusCode = StatusCode.ERROR_SERVER;
        }


        logger.log(Level.INFO, "Finished handling command [{0}]. Responding with Status: {1}", new Object[]{commandName, statusCode});
        return new Response(statusCode, responseBody, errorMessage); // Use the 3-arg constructor
    }


    /**
     * Checks if the provided argument is an instance of the expected type.
     * Throws IllegalArgumentException if the type is incorrect or if a non-null argument was expected but null was received.
     * @param argument The argument object received in the request.
     * @param expectedType The Class the argument is expected to be.
     * @param commandName The name of the command for error messages.
     * @throws IllegalArgumentException If validation fails.
     */
    private void ensureArgumentType(Object argument, Class<?> expectedType, String commandName) throws IllegalArgumentException {
        if (argument == null) {
            throw new IllegalArgumentException("Missing required " + expectedType.getSimpleName() + " argument for command '" + commandName + "'.");
        }
        if (!expectedType.isInstance(argument)) {

            throw new IllegalArgumentException("Invalid argument type for command '" + commandName + "'. Expected " + expectedType.getSimpleName() + ", but got " + argument.getClass().getSimpleName());
        }

        logger.finest("Argument type validated successfully for " + commandName + ": Expected " + expectedType.getSimpleName() + ", Got " + argument.getClass().getSimpleName());
    }

    /**
     * Validates the specific structure required for the 'update' command's argument.
     * Expects an Object[] containing exactly [Integer id, StudyGroup data].
     * @param argument The argument object received.
     * @throws IllegalArgumentException If validation fails.
     */
    private void validateUpdateArgs(Object argument) throws IllegalArgumentException {
        logger.finest("Validating specific arguments for 'update' command...");
        if (!(argument instanceof Object[])) {
            throw new IllegalArgumentException("Update argument must be an Object array.");
        }
        Object[] args = (Object[]) argument;
        if (args.length != 2) {
            throw new IllegalArgumentException("Update argument array must have exactly 2 elements ([Integer ID, StudyGroup data]). Found " + args.length);
        }
        if (!(args[0] instanceof Integer)) {
            String type = (args[0] == null) ? "null" : args[0].getClass().getSimpleName();
            throw new IllegalArgumentException("Update first argument (ID) must be an Integer. Found: " + type);
        }
        if (!(args[1] instanceof StudyGroup)) {
            String type = (args[1] == null) ? "null" : args[1].getClass().getSimpleName();
            throw new IllegalArgumentException("Update second argument (data) must be a StudyGroup. Found: " + type);
        }
        logger.finest("'update' arguments validated successfully.");
    }


    /**
     * Generates the help text describing available server commands.
     * @return A formatted help string.
     */
    private String getHelpText() {

        return """
               Available Commands (Client -> Server):
                 help                                    : Show this help
                 info                                    : Show collection info
                 show                                    : Show elements (sorted by student count)
                 add {element}                           : Add element (client builds element interactively)
                 update <id> {element}                   : Update element by ID (client builds element interactively)
                 remove_by_id <id>                       : Remove element by ID
                 clear                                   : Clear the collection
                 add_if_min {element}                    : Add element if it's smaller than the minimum (by ID)
                 remove_lower {element}                  : Remove elements smaller than the given one (by ID)
                 remove_any_by_form_of_education <form>  : Remove one element by FormOfEducation
                 print_ascending                         : Show elements sorted by ID
                 print_field_ascending_group_admin       : Show group admins sorted by name
               """;
    }
}