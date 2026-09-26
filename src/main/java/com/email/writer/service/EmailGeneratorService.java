package com.email.writer.service;

import com.email.writer.emaildto.EmailRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    public EmailGeneratorService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public String generateEmailReply(EmailRequestDto emailRequestDto){

        System.out.println("API key exists: " + (geminiApiKey != null));
        System.out.println("API key length: " + geminiApiKey.length());

        String prompt = buildPrompt(emailRequestDto);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of(
                                "parts", new Object[] {
                                        Map.of(
                                                "text", prompt
                                        )
                                }
                        )
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl)
                .header("x-goog-api-key", geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .exchangeToMono(responses -> {

                    System.out.println("Gemini HTTP status: " + responses.statusCode());

                    return responses.bodyToMono(String.class)
                            .doOnNext(body -> System.out.println("Gemini response: " + body));
                })
                .block();

        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            // Check if Gemini returned an error
            if (rootNode.has("error")) {
                return "Gemini API Error: "
                        + rootNode.path("error")
                        .path("message")
                        .asText();
            }

            // Check if candidates exist
            JsonNode candidates = rootNode.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                return "Gemini returned no candidates.";
            }

            JsonNode parts = candidates.get(0)
                    .path("content")
                    .path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                return "Gemini returned no response text.";
            }

            JsonNode textNode = parts.get(0).path("text");

            return textNode.asText();

        } catch (Exception e) {
            return "Error Processing Request: " + e.getMessage();
        }
    }
    private String buildPrompt(EmailRequestDto emailRequestDto) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line.");

        if(emailRequestDto.getTone() != null && !emailRequestDto.getTone().isEmpty()){
            prompt.append("Use a ").append(emailRequestDto.getTone()).append(" tone");
        }
        prompt.append("\nOriginal email : \n").append(emailRequestDto.getEmailContent());

        return prompt.toString();
    }
}
