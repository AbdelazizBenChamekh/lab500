package org.example.server.exceptions;

import java.io.IOException;

/**
 * Exception class for command runtime errors.
 */
public class CommandRuntimeError extends IOException {
    public CommandRuntimeError() {
        super();
    }

    public CommandRuntimeError(String message) {
        super(message);
    }

    public CommandRuntimeError(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandRuntimeError(Throwable cause) {
        super(cause);
    }
}
