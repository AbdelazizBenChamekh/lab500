package org.example.server.exceptions;

import java.io.IOException;

/**
 * Exception class for incorrectly filled form
 */
public class InvalidForm extends Exception {
    public InvalidForm(String message) {
        super(message);
    }
}