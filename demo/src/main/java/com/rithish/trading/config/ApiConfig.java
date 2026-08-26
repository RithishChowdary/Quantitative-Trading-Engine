package com.rithish.trading.config;

import lombok.Getter;

@Getter
public final class ApiConfig {

    private final String apiKey;
    private final String baseUrl;

    public ApiConfig(String apiKey, String baseUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key must not be empty");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL must not be empty");
        }

        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }
}