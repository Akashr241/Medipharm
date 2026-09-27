package com.example.demo.ai.chatbot.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Component
public class GeminiClient {

        private static final Logger logger =
                        LoggerFactory.getLogger(GeminiClient.class);
        private static final int MAX_ATTEMPTS = 3;
        private static final long INITIAL_RETRY_DELAY_MS = 1000;

    @Value("${gemini.api.key}")
    private String apiKey;

    public String askGemini(String prompt) {
                for (int attempt = 1; ; attempt++) {
                        try {
                                return generateContent(prompt);
                        } catch (RuntimeException exception) {
                                if (attempt >= MAX_ATTEMPTS || !isTransient(exception)) {
                                        throw exception;
                                }

                                long delayMillis = INITIAL_RETRY_DELAY_MS * (1L << (attempt - 1));
                                logger.warn(
                                                "Transient Gemini failure on attempt {}/{}; retrying in {} ms",
                                                attempt,
                                                MAX_ATTEMPTS,
                                                delayMillis
                                );

                                try {
                                        waitBeforeRetry(delayMillis);
                                } catch (InterruptedException interruptedException) {
                                        Thread.currentThread().interrupt();
                                        throw new IllegalStateException(
                                                        "Interrupted while waiting to retry Gemini request",
                                                        interruptedException
                                        );
                                }
                        }
                }
        }

        protected String generateContent(String prompt) {
        Client client = Client.builder()
                .apiKey(apiKey)
                .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.6-flash",
                        prompt,
                        null
                );

        return response.text();
    }

        protected void waitBeforeRetry(long delayMillis)
                        throws InterruptedException {
                Thread.sleep(delayMillis);
        }

        private boolean isTransient(Throwable exception) {
                for (Throwable current = exception;
                                current != null;
                                current = current.getCause()) {
                        String message = current.getMessage();
                        if (message == null) {
                                continue;
                        }

                        String normalizedMessage = message.toLowerCase();
                        if (normalizedMessage.contains("503")
                                        || normalizedMessage.contains("429")
                                        || normalizedMessage.contains("unavailable")
                                        || normalizedMessage.contains("high demand")
                                        || normalizedMessage.contains("try again later")) {
                                return true;
                        }
                }

                return false;
        }
}