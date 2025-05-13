package org.example.common.network;
import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String commandName;
    private final Object commandArgument;

    public Request(String commandName) {
        this.commandName = commandName;
        this.commandArgument = null;
    }

    public Request(String commandName, Object commandArgument) {
        this.commandName = commandName;
        this.commandArgument = commandArgument;
    }


    public String getCommandName() { return commandName; }
    public Object getArgument() { return commandArgument; } // <<< Changed getter name/type


    public boolean isEmpty() { // Check only command name
        return commandName == null || commandName.trim().isEmpty();
    }
}