package org.example.common.network;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a response sent from the server to the client.
 * Contains status, optional body (results), and optional error message.
 * Must be Serializable.
 */
public class Response implements Serializable {

    @Serial
    private static final long serialVersionUID = 6529685098267757691L; // Example value

    private final StatusCode statusCode;
    private final Object responseBody;
    private final String errorMessage;

    /**
     * Constructor for creating a Response.
     * @param statusCode The status code indicating the outcome.
     * @param responseBody The data result (can be null).
     * @param errorMessage The error message (should be null if statusCode is OK).
     */
    public Response(StatusCode statusCode, Object responseBody, String errorMessage) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.errorMessage = errorMessage;
    }

    /** Gets the status code of the response. */
    public StatusCode getStatusCode() {
        return statusCode;
    }

    /** Gets the data payload of the response. */
    public Object getResponseBody() {
        return responseBody;
    }

    /** Gets the error message, if any. */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** Convenience method to check if the operation was successful. */
    public boolean isSuccess() {
        return statusCode == StatusCode.OK;
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", responseBody=" + (responseBody != null ? responseBody.getClass().getSimpleName() : "null") + // Avoid printing large bodies
                ", errorMessage='" + (errorMessage == null ? "" : errorMessage) + '\'' +
                '}';
    }
}
