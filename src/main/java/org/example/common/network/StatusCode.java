package org.example.common.network;

import java.io.Serializable;

/**
 * Represents the status outcome of processing a request on the server.
 * Sent back to the client within the Response object.
 * Must be Serializable to be sent over the network.
 */
public enum StatusCode implements Serializable {
    /** Operation completed successfully. Response body might contain results. */
    OK,
    /** A general error occurred during processing on the server. Check error message. */
    ERROR,
    /** The requested resource (e.g., element by ID) was not found. */
    ERROR_NOT_FOUND,
    /** An unexpected internal error occurred on the server. */
    ERROR_SERVER,
    /** The command name sent by the client is not recognized by the server. */
    ERROR_UNKNOWN_COMMAND,
    /** The command sent is recognized but should be handled client-side (e.g., history, exit). */
    ERROR_CLIENT_COMMAND
}
