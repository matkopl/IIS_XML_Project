package hr.algebra.client.controller;

import hr.algebra.client.service.SoapService;
import hr.algebra.client.util.XmlUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainController {
    private final SoapService soapService = new SoapService();
    private final XmlUtils xmlUtils = new XmlUtils();
    private String accessToken;
    private String refreshToken;
    private String accessTokenExpiry;

    @FXML
    private Tab crudTab, soapTab, jaxbTab, xmlRpcTab, validationTab;

    @FXML
    private AnchorPane crudPane, soapPane, jaxbPane, xmlRpcPane, validationPane;

    @FXML
    private TextArea xsdInputArea, xsdResultArea, xsdSchemaArea, rngInputArea, rngResultArea, rngSchemaArea, soapResultArea;

    @FXML
    private TextField soapTermField;

    public void initialize() {
        crudTab.setDisable(true);
        jaxbTab.setDisable(true);
        xsdSchemaArea.setText(loadResourceAsString("schemes/sport.xsd"));
        rngSchemaArea.setText(loadResourceAsString("schemes/sport.rng"));
    }

    public void enableCrudAndJaxbTab() {
        crudTab.setDisable(false);
        jaxbTab.setDisable(false);
    }

    public void setAuthData(String accessToken, String refreshToken, String accessTokenExpiry) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiry = accessTokenExpiry;
    }

    public String getAccessToken() {
        return accessToken;
    }

    private String loadResourceAsString(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream("/" + resourcePath)) {
            if (is == null) return "Shema nije pronađena: " + resourcePath;
            return new String(is.readAllBytes());
        } catch (IOException e) {
            return "Greška pri učitavanju: " + e.getMessage();
        }
    }

    @FXML
    public void onXsdValidate() {
        String xml = xsdInputArea.getText();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/xsd"))
                    .header("Content-Type", "application/xml")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(xml))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            xsdResultArea.setText(response.body());
                        });
                    });
        } catch (Exception e) {
            xsdResultArea.setText("Greška: " + e.getMessage());
        }
    }

    @FXML
    public void onRngValidate() {
        String xml = rngInputArea.getText();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/rng"))
                    .header("Content-Type", "application/xml")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString(xml))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        Platform.runLater(() -> {
                            rngResultArea.setText(response.body());
                        });
                    });
        } catch (Exception e) {
            rngResultArea.setText("Greška: " + e.getMessage());
        }
    }

    private boolean crudAndJaxbEnabled = false;

    @FXML
    public void onSoapSearch() {
        String term = soapTermField.getText();
        if (term == null || term.isBlank()) {
            soapResultArea.setText("Unesite termin za pretragu.");
            return;
        }
        if (accessToken == null) {
            soapResultArea.setText("Niste prijavljeni.");
            return;
        }
        soapResultArea.setText("Pretražujem...");
        soapService.searchSoap(term, accessToken)
                .thenAccept(result -> Platform.runLater(() -> {
                    soapResultArea.setText(xmlUtils.prettyFormatXml(result));
                    if (!crudAndJaxbEnabled) {
                        enableCrudAndJaxbTab();
                        crudAndJaxbEnabled = true;
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> soapResultArea.setText("Greška: " + ex.getMessage()));
                    return null;
                });
    }

}
