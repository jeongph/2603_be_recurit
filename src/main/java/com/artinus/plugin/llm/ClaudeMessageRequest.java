package com.artinus.plugin.llm;

import java.util.List;

record ClaudeMessageRequest(String model, int max_tokens, List<Message> messages) {
    record Message(String role, String content) {}
}
