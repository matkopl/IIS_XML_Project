package hr.algebra.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private String accessToken;
    private String refreshToken;
    private Instant accessTokenExpiry;

    @FXML
    protected void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        this.accessToken = extractJsonValue(response.body(), "accessToken");
                        this.refreshToken = extractJsonValue(response.body(), "refreshToken");
                        this.accessTokenExpiry = parseJwtExpiry(accessToken);

                        Platform.runLater(() -> {
                            statusLabel.setText("Uspješna prijava!");

                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/algebra/client/MainView.fxml"));
                                Scene mainScene = new Scene(loader.load(), 900, 500);
                                MainController controller = loader.getController();
                                controller.setAuthData(accessToken, refreshToken, String.valueOf(accessTokenExpiry));

                                Stage stage = (Stage) usernameField.getScene().getWindow();
                                stage.setScene(mainScene);
                                stage.setTitle("Sportski sustav");
                                stage.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            statusLabel.setText("Neuspješna prijava!");
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> statusLabel.setText("Greška: " + ex.getMessage()));
                    return null;
                });
    }

    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }


    private Instant parseJwtExpiry(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));
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

    private void ensureValidAccessToken() {
        if (accessTokenExpiry != null || Instant.now().isBefore(accessTokenExpiry.minusSeconds(30))) {
            refreshAccessToken();
        }
    }

    private void refreshAccessToken() {
        String json = String.format("{\"refreshToken\":\"%s\"}", refreshToken);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/refresh"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                this.accessToken = extractJsonValue(response.body(), "accessToken");
                this.accessTokenExpiry = parseJwtExpiry(accessToken);
            } else {
                Platform.runLater(() -> statusLabel.setText("Refresh token je nevažeći! Prijavite se ponovno."));
            }
        } catch (Exception e) {
            Platform.runLater(() -> statusLabel.setText("Greška kod refreshanja tokena: " + e.getMessage()));
        }
    }

    public String getValidAccessToken() {
        ensureValidAccessToken();
        return accessToken;
    }
}
