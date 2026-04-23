package com.artinus.plugin.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record ClaudeMessageResponse(List<Content> content) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(String type, String text) {}
}
