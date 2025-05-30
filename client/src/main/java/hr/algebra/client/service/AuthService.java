package hr.algebra.client.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthService {
    private String accessToken;
    private String refreshToken;
    private Instant accessTokenExpiration;

    public boolean login(String username, String password) {
        try {
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                this.accessToken = extractJsonValue(response.body(), "accessToken");
                this.refreshToken = extractJsonValue(response.body(), "refreshToken");
                this.accessTokenExpiration = parseJwtExpiry(accessToken);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Greška kod prijave: " + e.getMessage());
        }
    }

    public void setTokens(String accessToken, String refreshToken, Instant accessTokenExpiration) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public synchronized String getValidAccessToken() {
        if (accessToken == null || Instant.now().isAfter(accessTokenExpiration.minusSeconds(5))) {
            refreshAccessToken();
        }

        return accessToken;
    }

    private void refreshAccessToken() {
        try {
            String json = String.format("{\"refreshToken\":\"%s\"}", refreshToken);
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/refresh"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                this.accessToken = extractJsonValue(response.body(), "accessToken");
                this.accessTokenExpiration = parseJwtExpiry(accessToken);
            } else {
                throw new RuntimeException("Refresh token je nevažeći! Prijavite se ponovno.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Greška kod refreshanja tokena: " + e.getMessage());
        }
    }

    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }


    private Instant parseJwtExpiry(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            Pattern pattern = Pattern.compile("\"exp\":(\\d+)");
            Matcher matcher = pattern.matcher(payload);
            if (matcher.find()) {
                long exp = Long.parseLong(matcher.group(1));
                return Instant.ofEpochSecond(exp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
