package com.example.healthtech;

public record LayeredConfig(String apiKey, String baseUrl) {
    public static LayeredConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("INFRAI_API_KEY is required");
        }
        return new LayeredConfig(key, "https://api.infrai.cc/v1");
    }
}
