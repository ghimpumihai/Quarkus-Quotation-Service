package org.stef.exception;

public class DownstreamBadRequestException extends GatewayException {

    public DownstreamBadRequestException(String message) {
        super(message, 400);
    }
}