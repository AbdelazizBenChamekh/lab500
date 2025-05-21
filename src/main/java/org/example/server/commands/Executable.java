package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.server.exceptions.CommandRuntimeError;
import org.example.server.exceptions.ExitObliged;
import org.example.server.exceptions.IllegalArguments;

/**
 * Interface for executable commands
 */
public interface Executable {
    Response execute(Request request) throws CommandRuntimeError, ExitObliged, IllegalArguments;
}