package com.artinus.plugin.llm;

import com.artinus.history.service.port.SummarizerUnavailableException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
@Profile("real-llm")
class ClaudeApiClient {

    private final RestClient restClient;
    private final ClaudeProperties properties;

    ClaudeApiClient(ClaudeProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout((int) properties.timeout().toMillis());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }

    String generateMessage(String prompt) {
        try {
            ClaudeMessageRequest req = new ClaudeMessageRequest(
                    properties.model(),
                    properties.maxTokens(),
                    List.of(new ClaudeMessageRequest.Message("user", prompt)));
            ClaudeMessageResponse body = restClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", properties.apiKey())
                    .header("anthropic-version", "2023-06-01")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(req)
                    .retrieve()
                    .body(ClaudeMessageResponse.class);

            if (body == null || body.content() == null || body.content().isEmpty()) {
                throw new SummarizerUnavailableException("claude returned empty response", null);
            }
            return body.content().stream()
                    .filter(c -> "text".equals(c.type()))
                    .map(ClaudeMessageResponse.Content::text)
                    .findFirst()
                    .orElseThrow(() -> new SummarizerUnavailableException("no text content", null));
        } catch (RestClientException e) {
            throw new SummarizerUnavailableException("claude call failed", e);
        }
    }
}
