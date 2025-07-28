package com.fitnessai.aiservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.Exceptions;

import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient;

    // Use a base URL for the API endpoint structure
    @Value("${gemini.api.base-url}")  // Make sure this is "https://generativelanguage.googleapis.com/v1beta"
    private String geminiApiBaseUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.model}") // e.g., gemini-1.5-flash
    private String geminiApiModel;


    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getAnswer(String question) {
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", question)
                        })
                }
        );

        // CORRECTED: Construct the URI by combining base-url, model, and appending key as query param
        String fullUri = String.format("%s/models/%s:generateContent?key=%s",
                geminiApiBaseUrl, geminiApiModel, geminiApiKey);

        try {
            String response = webClient.post()
                    .uri(fullUri) // Use the correctly constructed full URI
                    .header("Content-Type", "application/json") // REMOVED Authorization header
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        System.err.println("Gemini API Error Response (Status " + clientResponse.statusCode() + "): " + errorBody);
                                        return Mono.error(new WebClientResponseException(
                                                "Gemini API returned an error: " + errorBody,
                                                clientResponse.statusCode().value(),
                                                clientResponse.statusCode().toString(),
                                                clientResponse.headers().asHttpHeaders(),
                                                errorBody.getBytes(),
                                                null
                                        ));
                                    })
                    )
                    .bodyToMono(String.class)
                    .block();
            return response;
        } catch (RuntimeException e) {
            Throwable cause = Exceptions.unwrap(e);
            System.err.println("Error communicating with Gemini API: " + cause.getMessage());
            if (cause instanceof WebClientResponseException) {
                WebClientResponseException wcException = (WebClientResponseException) cause;
                System.err.println("HTTP Status: " + wcException.getStatusCode());
                System.err.println("Response Body: " + wcException.getResponseBodyAsString());
            }
            throw new RuntimeException("Failed to get answer from Gemini API", cause);
        }
    }


    // Reactive version (recommended)
    public Mono<String> getAnswerReactive(String question) {
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", question)
                        })
                }
        );

        String fullUri = String.format("%s/models/%s:generateContent?key=%s",
                geminiApiBaseUrl, geminiApiModel, geminiApiKey);

        return webClient.post()
                .uri(fullUri)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    System.err.println("Gemini API Reactive Error Response (Status " + clientResponse.statusCode() + "): " + errorBody);
                                    return Mono.error(new WebClientResponseException(
                                            "Gemini API returned an error: " + errorBody,
                                            clientResponse.statusCode().value(),
                                            clientResponse.statusCode().toString(),
                                            clientResponse.headers().asHttpHeaders(),
                                            errorBody.getBytes(),
                                            null
                                    ));
                                })
                )
                .bodyToMono(String.class);
    }
}
