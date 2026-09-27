package com.example.demo.ai.chatbot.client;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeminiClientTest {

    @Test
    void retriesTemporary503AndReturnsSuccessfulResponse() {
        AtomicInteger attempts = new AtomicInteger();
        GeminiClient client = new GeminiClient() {
            @Override
            protected String generateContent(String model, String prompt) {
                if (attempts.incrementAndGet() < 3) {
                    throw new RuntimeException("503 Service Unavailable: high demand");
                }
                return "response";
            }

            @Override
            protected void waitBeforeRetry(long delayMillis) {
            }
        };

        assertEquals("response", client.askGemini("prompt"));
        assertEquals(3, attempts.get());
    }

    @Test
    void doesNotRetryPermanentFailures() {
        AtomicInteger attempts = new AtomicInteger();
        GeminiClient client = new GeminiClient() {
            @Override
            protected String generateContent(String model, String prompt) {
                attempts.incrementAndGet();
                throw new RuntimeException("400 Bad Request");
            }
        };

        assertThrows(RuntimeException.class, () -> client.askGemini("prompt"));
        assertEquals(1, attempts.get());
    }

    @Test
    void usesFallbackModelAfterPrimaryModelRemainsUnavailable() {
        AtomicInteger primaryAttempts = new AtomicInteger();
        AtomicInteger fallbackAttempts = new AtomicInteger();
        GeminiClient client = new GeminiClient() {
            @Override
            protected String generateContent(String model, String prompt) {
                if (model.equals("gemini-3.8-flash")) {
                    primaryAttempts.incrementAndGet();
                    throw new RuntimeException("503 Service Unavailable: high demand");
                }
                if (model.equals("gemini-3.7-flash")) {
                    fallbackAttempts.incrementAndGet();
                    return "fallback response";
                }
                throw new AssertionError("Unexpected Gemini model: " + model);
            }

            @Override
            protected void waitBeforeRetry(long delayMillis) {
            }
        };

        assertEquals("fallback response", client.askGemini("prompt"));
        assertEquals(3, primaryAttempts.get());
        assertEquals(1, fallbackAttempts.get());
    }

    @Test
    void triesNextFallbackWhenFirstFallbackAlsoReturns503() {
        AtomicInteger primaryAttempts = new AtomicInteger();
        AtomicInteger firstFallbackAttempts = new AtomicInteger();
        AtomicInteger secondFallbackAttempts = new AtomicInteger();
        GeminiClient client = new GeminiClient() {
            @Override
            protected String generateContent(String model, String prompt) {
                if (model.equals("gemini-3.8-flash")) {
                    primaryAttempts.incrementAndGet();
                    throw new RuntimeException("503 Service Unavailable: high demand");
                }
                if (model.equals("gemini-3.7-flash")) {
                    firstFallbackAttempts.incrementAndGet();
                    throw new RuntimeException("503 Service Unavailable: high demand");
                }
                if (model.equals("gemini-3.6-flash")) {
                    secondFallbackAttempts.incrementAndGet();
                    return "second fallback response";
                }
                throw new AssertionError("Unexpected Gemini model: " + model);
            }

            @Override
            protected void waitBeforeRetry(long delayMillis) {
            }
        };

        assertEquals("second fallback response", client.askGemini("prompt"));
        assertEquals(3, primaryAttempts.get());
        assertEquals(1, firstFallbackAttempts.get());
        assertEquals(1, secondFallbackAttempts.get());
    }
}