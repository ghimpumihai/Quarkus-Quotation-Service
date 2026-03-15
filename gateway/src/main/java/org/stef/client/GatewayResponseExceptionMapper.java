package org.stef.client;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.stef.exception.DownstreamBadRequestException;
import org.stef.exception.DownstreamNotFoundException;
import org.stef.exception.DownstreamServiceException;

public class GatewayResponseExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    @Override
    public RuntimeException toThrowable(Response response) {
        int status = response.getStatus();

        if (status == 404) {
            return new DownstreamNotFoundException("Requested resource not found in downstream service");
        }
        if (status == 400) {
            return new DownstreamBadRequestException("Invalid request sent to downstream service");
        }
        if (status >= 500) {
            return new DownstreamServiceException("Downstream service returned error (HTTP " + status + ")");
        }
        return new DownstreamServiceException("Unexpected response from downstream service (HTTP " + status + ")");
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status >= 400;
    }
}