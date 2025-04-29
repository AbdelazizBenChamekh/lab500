package org.example.common.network;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a request sent from the client to the server.
 * Contains the command name and an optional argument object.
 * Must be Serializable.
 */
public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 6529685098267757690L; // Example value

    private final String commandName;
    private final Object commandArgument; // Can hold ID (Integer), StudyGroup, FormOfEducation, Object[], null etc.

    /**
     * Constructor for creating a Request.
     * @param commandName The name of the command to execute.
     * @param commandArgument The argument object (can be null).
     */
    public Request(String commandName, Object commandArgument) {
        this.commandName = commandName;
        this.commandArgument = commandArgument;
    }

    /** Gets the name of the command. */
    public String getCommandName() {
        return commandName;
    }

    /** Gets the argument object associated with the command. */
    public Object getCommandArgument() {
        return commandArgument;
    }

    @Override
    public String toString() {
        return "Request{" +
                "commandName='" + commandName + '\'' +
                ", commandArgument=" + (commandArgument != null ? commandArgument.getClass().getSimpleName() : "null") + // Avoid printing large objects
                '}';
    }
}
