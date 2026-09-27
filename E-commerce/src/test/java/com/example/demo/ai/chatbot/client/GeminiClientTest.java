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
            protected String generateContent(String prompt) {
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
            protected String generateContent(String prompt) {
                attempts.incrementAndGet();
                throw new RuntimeException("400 Bad Request");
            }
        };

        assertThrows(RuntimeException.class, () -> client.askGemini("prompt"));
        assertEquals(1, attempts.get());
    }
}