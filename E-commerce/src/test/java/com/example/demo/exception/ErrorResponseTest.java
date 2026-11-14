package com.example.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    void constructorWithoutTimestampSetsMessageStatusAndTimestamp() {
        ErrorResponse response = new ErrorResponse("Not Found", 404);

        assertEquals("Not Found", response.getMessage());
        assertEquals(404, response.getStatus());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void constructorUsesProvidedTimestamp() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 28, 10, 30);

        ErrorResponse response = new ErrorResponse("Bad Request", 400, timestamp);

        assertEquals("Bad Request", response.getMessage());
        assertEquals(400, response.getStatus());
        assertEquals(timestamp, response.getTimestamp());
    }
}
