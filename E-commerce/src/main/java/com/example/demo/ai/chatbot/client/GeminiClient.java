package com.example.demo.ai.chatbot.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class GeminiClient {

        private static final Logger logger =
                        LoggerFactory.getLogger(GeminiClient.class);
        private static final int MAX_ATTEMPTS = 3;
        private static final long INITIAL_RETRY_DELAY_MS = 1000;

    @Value("${gemini.api.key}")
    private String apiKey;

        @Value("${gemini.api.model:gemini-3.8-flash}")
        private String primaryModel = "gemini-3.8-flash";

    @Value("${gemini.api.fallback-models:${gemini.api.fallback-model:gemini-3.8-flash,gemini-3.7-flash,gemini-3.5-flash-lite}}")
    private String fallbackModels =
            "gemini-3.7-flash,gemini-3.6-flash,gemini-3.5-flash-lite";

    public String askGemini(String prompt) {
        RuntimeException primaryFailure = null;
                for (int attempt = 1; ; attempt++) {
                        try {
                                return generateContent(primaryModel, prompt);
                        } catch (RuntimeException exception) {
                                if (attempt >= MAX_ATTEMPTS || !isTransient(exception)) {
                                        if (!isTransient(exception)) {
                                                throw exception;
                                        }
                                        primaryFailure = exception;
                                        break;
                                }

                                long delayMillis = INITIAL_RETRY_DELAY_MS * (1L << (attempt - 1));
                                logger.warn(
                                                "Transient Gemini failure for model {} on attempt {}/{}; retrying in {} ms",
                                                primaryModel,
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

        Set<String> attemptedModels = new LinkedHashSet<>();
        attemptedModels.add(primaryModel);
        List<RuntimeException> transientFailures = new ArrayList<>();
        transientFailures.add(primaryFailure);

        for (String configuredModel : fallbackModels.split(",")) {
                String fallbackModel = configuredModel.trim();
                if (fallbackModel.isEmpty() || !attemptedModels.add(fallbackModel)) {
                        continue;
                }

                logger.warn(
                        "Primary Gemini model {} remained unavailable after {} attempts; trying fallback model {}",
                        primaryModel,
                        MAX_ATTEMPTS,
                        fallbackModel
                );
                try {
                        return generateContent(fallbackModel, prompt);
                } catch (RuntimeException fallbackFailure) {
                        if (!isTransient(fallbackFailure)) {
                                transientFailures.forEach(fallbackFailure::addSuppressed);
                                throw fallbackFailure;
                        }

                        transientFailures.add(fallbackFailure);
                        logger.warn(
                                "Fallback Gemini model {} is temporarily unavailable",
                                fallbackModel
                        );
                }
        }

        RuntimeException lastFailure =
                transientFailures.get(transientFailures.size() - 1);
        IllegalStateException allModelsUnavailable = new IllegalStateException(
                "All configured Gemini models are temporarily unavailable: "
                        + String.join(", ", attemptedModels),
                lastFailure
        );
        transientFailures.stream()
                .filter(failure -> failure != lastFailure)
                .forEach(allModelsUnavailable::addSuppressed);
        throw allModelsUnavailable;
        }

        protected String generateContent(String model, String prompt) {
        Client client = Client.builder()
                .apiKey(apiKey)
                .build();

        GenerateContentResponse response =
                client.models.generateContent(
                        model,
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