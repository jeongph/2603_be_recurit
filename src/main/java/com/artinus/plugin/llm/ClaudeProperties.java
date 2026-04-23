package com.artinus.plugin.llm;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "llm.claude")
public record ClaudeProperties(
        String baseUrl,
        String apiKey,
        String model,
        int maxTokens,
        Duration timeout
) {
    public ClaudeProperties {
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "https://api.anthropic.com";
        if (model == null || model.isBlank()) model = "claude-haiku-4-5-20251001";
        if (maxTokens <= 0) maxTokens = 1024;
        if (timeout == null) timeout = Duration.ofSeconds(10);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "llm.claude.api-key 가 설정되지 않았습니다. "
                            + "real-llm 프로파일을 활성화하려면 ANTHROPIC_API_KEY 환경변수를 주입하세요.");
        }
    }
}
