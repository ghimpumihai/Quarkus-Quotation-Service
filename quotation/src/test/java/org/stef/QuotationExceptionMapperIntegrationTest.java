package org.stef;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;


@QuarkusTest
class QuotationExceptionMapperIntegrationTest {

    @Test
    void shouldReturn503WithCorrectBodyWhenProviderUnavailable() {
        given()
                .when().get("/test/exception/provider")
                .then()
                .statusCode(503)
                .contentType(MediaType.APPLICATION_JSON)
                .body("status", equalTo(503))
                .body("error", equalTo("Service Unavailable"))
                .body("message", equalTo("Currency API is unavailable (HTTP 503)"));
    }

    @Test
    void shouldReturn400WithCorrectBodyWhenInvalidCurrencyCode() {
        given()
                .when().get("/test/exception/currency")
                .then()
                .statusCode(400)
                .contentType(MediaType.APPLICATION_JSON)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Invalid or unsupported currency code: USD-BRL"));
    }
}