package org.stef.exception;

public class DownstreamNotFoundException extends GatewayException {

    public DownstreamNotFoundException(String message) {
        super(message, 404);
    }
}