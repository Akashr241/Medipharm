package com.example.demo.exception;

import java.time.LocalDateTime;

public class ErrorResponseManualTest {

    public static void main(String[] args) {
        testDefaultConstructor();
        testCustomTimestampConstructor();
        testExceptionSafeCreation();
        System.out.println("All normal Java tests passed.");
    }

    private static void testDefaultConstructor() {
        ErrorResponse response = new ErrorResponse("Not Found", 404);

        if (!"Not Found".equals(response.getMessage())) {
            throw new AssertionError("Message mismatch");
        }

        if (response.getStatus() != 404) {
            throw new AssertionError("Status mismatch");
        }

        if (response.getTimestamp() == null) {
            throw new AssertionError("Timestamp should not be null");
        }
    }

    private static void testCustomTimestampConstructor() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 28, 10, 30);

        ErrorResponse response = new ErrorResponse("Bad Request", 400, now);

        if (!"Bad Request".equals(response.getMessage())) {
            throw new AssertionError("Message mismatch");
        }

        if (response.getStatus() != 400) {
            throw new AssertionError("Status mismatch");
        }

        if (!now.equals(response.getTimestamp())) {
            throw new AssertionError("Timestamp mismatch");
        }
    }

    private static void testExceptionSafeCreation() {
        try {
            new ErrorResponse("Unauthorized", 401);
            new ErrorResponse("Forbidden", 403, LocalDateTime.now());
            System.out.println("No exceptions thrown for valid data.");
        } catch (Exception e) {
            throw new AssertionError("Valid ErrorResponse creation should not throw exception: " + e.getMessage());
        }
    }
}
