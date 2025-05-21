package org.example.client.commandLine;

import org.example.client.Exception.CommandRuntimeError;
import org.example.client.Exception.ExitObliged;
import org.example.client.Exception.IllegalArguments;

/**
 * Interface for executable commands
 */
public interface Executable {
    void execute(String args) throws CommandRuntimeError, ExitObliged, IllegalArguments;
}