package com.stanislav.vgt_chat_bot.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class OpenAiAnalysisService {
    @Value("${openai.apiKey:}")
    String apiKey;
    @Value("${openai.model:gpt-4o-mini}")
    String model;

    private final ObjectMapper mapper = new ObjectMapper();

    private WebClient client() {
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Analysis analyze(String text) {
        var sys = Map.of("role", "system", "content", "You output only strict JSON.");
        var userPrompt = """
You are a classifier for service-station employee feedback.
Return STRICT JSON with fields:
sentiment: NEGATIVE|NEUTRAL|POSITIVE,
criticality: integer 1..5,
resolution: short Ukrainian advice how to solve the issue.
Text: \"%s\"
JSON only.
""".formatted(text);
        var user = Map.of("role", "user", "content", userPrompt);
        var body = Map.of(
                "model", model,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(sys, user)
        );


        try {
            var resp = client().post().uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (resp == null) throw new RuntimeException("OpenAI empty response");
            var choices = (List<Map<String,Object>>) resp.get("choices");
            var message = (Map<String,Object>) choices.get(0).get("message");
            var content = (String) message.get("content");
            JsonNode n = mapper.readTree(content);
            String sentiment = n.path("sentiment").asText("NEUTRAL");
            short criticality = (short) n.path("criticality").asInt(3);
            String resolution = n.path("resolution").asText("");
            return new Analysis(sentiment, criticality, resolution);
        } catch (Exception e) {
            return new Analysis("NEUTRAL", (short)3, "Передати питання адміністратору відділення для перевірки.");
        }
    }

    public record Analysis(String sentiment, short criticality, String resolution) {}
}

