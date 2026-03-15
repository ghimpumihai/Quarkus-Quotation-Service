package org.stef;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class ProposalExceptionMapperIntegrationTest {

    @Test
    void shouldReturn404WithCorrectBodyWhenProposalNotFound() {
        given()
                .when().get("/test/proposal/exception/not-found")
                .then()
                .statusCode(404)
                .contentType("application/json")
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"))
                .body("message", equalTo("Proposal not found with id: 99"));
    }

    @Test
    void shouldReturn500WithCorrectBodyWhenProposalCreationFails() {
        given()
                .when().get("/test/proposal/exception/creation")
                .then()
                .statusCode(500)
                .contentType("application/json")
                .body("status", equalTo(500))
                .body("error", equalTo("Internal Server Error"))
                .body("message", equalTo("Failed to persist proposal"));
    }
}