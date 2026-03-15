package org.stef;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.stef.client.GatewayResponseExceptionMapper;
import org.stef.controller.ProposalController;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.exception.DownstreamBadRequestException;
import org.stef.exception.DownstreamNotFoundException;
import org.stef.exception.DownstreamServiceException;
import org.stef.service.ProposalService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GatewayExceptionUnitTest {

    private final GatewayResponseExceptionMapper mapper = new GatewayResponseExceptionMapper();

    @Test
    void shouldMap404ToDownstreamNotFoundException() {
        Response response = Response.status(404).build();

        RuntimeException ex = mapper.toThrowable(response);

        assertInstanceOf(DownstreamNotFoundException.class, ex);
        assertEquals(404, ((DownstreamNotFoundException) ex).getStatusCode());
    }

    @Test
    void shouldMap400ToDownstreamBadRequestException() {
        Response response = Response.status(400).build();

        RuntimeException ex = mapper.toThrowable(response);

        assertInstanceOf(DownstreamBadRequestException.class, ex);
        assertEquals(400, ((DownstreamBadRequestException) ex).getStatusCode());
    }

    @Test
    void shouldMap500ToDownstreamServiceException() {
        Response response = Response.status(500).build();

        RuntimeException ex = mapper.toThrowable(response);

        assertInstanceOf(DownstreamServiceException.class, ex);
        assertEquals(503, ((DownstreamServiceException) ex).getStatusCode());
    }

    @Test
    void shouldMap503ToDownstreamServiceException() {
        Response response = Response.status(503).build();

        RuntimeException ex = mapper.toThrowable(response);

        assertInstanceOf(DownstreamServiceException.class, ex);
        assertEquals(503, ((DownstreamServiceException) ex).getStatusCode());
    }

    @Test
    void shouldHandleAllStatusesAbove400() {
        assertTrue(mapper.handles(400, null));
        assertTrue(mapper.handles(404, null));
        assertTrue(mapper.handles(500, null));
        assertTrue(mapper.handles(503, null));
    }

    @Test
    void shouldNotHandleSuccessStatuses() {
        assertTrue(!mapper.handles(200, null));
        assertTrue(!mapper.handles(201, null));
        assertTrue(!mapper.handles(204, null));
    }

    @Test
    void downstreamNotFoundExceptionHasCorrectStatusCode() {
        DownstreamNotFoundException ex = new DownstreamNotFoundException("not found");
        assertEquals(404, ex.getStatusCode());
        assertEquals("not found", ex.getMessage());
    }

    @Test
    void downstreamServiceExceptionHasCorrectStatusCode() {
        DownstreamServiceException ex = new DownstreamServiceException("service down");
        assertEquals(503, ex.getStatusCode());
    }

    @Test
    void downstreamBadRequestExceptionHasCorrectStatusCode() {
        DownstreamBadRequestException ex = new DownstreamBadRequestException("bad request");
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void proposalControllerBubblesUpDownstreamNotFoundException() {
        ProposalService proposalService = mock(ProposalService.class);
        when(proposalService.getProposalDetailsById(99L))
                .thenThrow(new DownstreamNotFoundException("Requested resource not found in downstream service"));
        ProposalController controller = new ProposalController(proposalService);

        assertThrows(DownstreamNotFoundException.class,
                () -> controller.getProposalDetailsById(99L));
    }

    @Test
    void proposalControllerBubblesUpDownstreamServiceException() {
        ProposalService proposalService = mock(ProposalService.class);
        doThrow(new DownstreamServiceException("Downstream service returned error (HTTP 500)"))
                .when(proposalService).createProposal(any());
        ProposalController controller = new ProposalController(proposalService);

        assertThrows(DownstreamServiceException.class,
                () -> controller.createNewProposal(ProposalDetailsDTO.builder().customer("ACME").build()));
    }
}