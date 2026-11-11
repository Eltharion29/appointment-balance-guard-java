package com.example.healthtech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

public final class InfraiClient {
    private static final Pattern OK_FALSE = Pattern.compile("\\\"ok\\\"\\s*:\\s*false\\b");
    private static final Pattern OK_TRUE = Pattern.compile("\\\"ok\\\"\\s*:\\s*true\\b");
    private final LayeredConfig config;
    private final HttpClient http;

    public InfraiClient(LayeredConfig config) {
        this.config = config;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String request(String method, String path, String body) {
        for (int attempt = 0; attempt < 4; attempt++) {
            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(config.baseUrl() + normalizePath(path)))
                        .timeout(Duration.ofSeconds(20))
                        .header("Authorization", "Bearer " + config.apiKey())
                        .header("Content-Type", "application/json");
                HttpRequest request = builder.method(method, body == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                String envelope = response.body();
                if (OK_FALSE.matcher(envelope).find()) {
                    throw new InfraiException(errorCode(envelope), response.statusCode());
                }
                if (response.statusCode() == 429 && attempt < 3) {
                    Thread.sleep(retryMillis(response, attempt));
                    continue;
                }
                if (response.statusCode() >= 500) {
                    throw new IOException("service transport status " + response.statusCode());
                }
                if (!OK_TRUE.matcher(envelope).find()) {
                    throw new InfraiException("INVALID_RESPONSE_ENVELOPE", response.statusCode());
                }
                return envelope;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("request interrupted", e);
            } catch (IOException e) {
                throw new IllegalStateException("request failed", e);
            }
        }
        throw new IllegalStateException("request retry budget exhausted");
    }

    private static String normalizePath(String path) {
        if (path.startsWith("/v1/")) return path.substring(3);
        return path.startsWith("/") ? path : "/" + path;
    }

    private static long retryMillis(HttpResponse<String> response, int attempt) {
        return response.headers().firstValue("Retry-After").map(v -> Long.parseLong(v) * 1000L)
                .orElse(250L * (1L << attempt));
    }

    private static String errorCode(String envelope) {
        int marker = envelope.indexOf("\"code\":\"");
        if (marker < 0) return "INFRAI_ERROR";
        int start = marker + 8;
        int end = envelope.indexOf('"', start);
        return end < 0 ? "INFRAI_ERROR" : envelope.substring(start, end);
    }
}
