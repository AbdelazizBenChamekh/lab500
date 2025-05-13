package org.example.common.network;

import org.example.common.models.StudyGroup;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a response sent from the server back to the client.
 * Contains a status code, a body for successful responses (message or data),
 * optional collection data, and a specific error message for failures.
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 2L;

    private final StatusCode statusCode;
    private final String responseBody;
    private final List<StudyGroup> collectionData;
    private final String errorMessage;

    /** Private constructor - use static factory methods instead */
    private Response(StatusCode statusCode, String responseBody, List<StudyGroup> collectionData, String errorMessage) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.collectionData = collectionData;
        this.errorMessage = errorMessage;
    }


    /** Creates a simple success (OK) response with only a message body. */
    public static Response success(String message) {
        return new Response(StatusCode.OK, message, null, null);
    }

    /** Creates a success (OK) response with collection data and an optional message. */
    public static Response success(String message, List<StudyGroup> data) {
        return new Response(StatusCode.OK, message, data, null);
    }

    /** Creates a success (OK) response with only collection data. */
    public static Response success(List<StudyGroup> data) {
        return new Response(StatusCode.OK, null, data, null);
    }

    /** Creates a simple success (OK) response with no body (operation complete). */
    public static Response success() {
        return new Response(StatusCode.OK, null, null, null);
    }

    /** Creates an error response with a specific status code and error message. */
    public static Response error(StatusCode errorStatus, String errorMessage) {
        if (errorStatus == StatusCode.OK) { // Prevent misuse
            throw new IllegalArgumentException("Cannot create error response with OK status.");
        }
        return new Response(errorStatus, null, null, errorMessage);
    }

    /** Creates a generic error response with just a message. */
    public static Response error(String errorMessage) {
        // Default to generic ERROR status if specific one isn't provided
        return new Response(StatusCode.ERROR, null, null, errorMessage);
    }


    /** Checks if the response status code indicates success (OK). */
    public boolean isSuccess() {
        return this.statusCode == StatusCode.OK;
    }

    /** Gets the status code of the response. */
    public StatusCode getStatusCode() {
        return statusCode;
    }

    /** Gets the response body (usually for success messages or simple string data). */
    public String getResponseBody() {
        return responseBody;
    }

    /** Gets the collection data associated with the response (null if none). */
    public List<StudyGroup> getCollectionData() {
        return collectionData;
    }

    /** Gets the specific error message (null if the response is successful). */
    public String getErrorMessage() {
        return errorMessage;
    }
}