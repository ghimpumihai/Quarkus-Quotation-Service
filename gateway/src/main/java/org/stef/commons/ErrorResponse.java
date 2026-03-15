package org.stef.commons;

public class ErrorResponse {
    public int status;
    public String error;
    public String message;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }
}