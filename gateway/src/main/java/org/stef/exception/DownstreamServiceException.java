package org.stef.exception;

public class DownstreamServiceException extends GatewayException {

    public DownstreamServiceException(String message) {
        super(message, 503);
    }
}