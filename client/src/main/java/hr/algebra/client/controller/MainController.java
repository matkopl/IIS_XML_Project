package hr.algebra.client.controller;

import hr.algebra.client.dto.SportDto;
import hr.algebra.client.service.*;
import hr.algebra.client.util.XmlUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class MainController {
    private AuthService authService = new AuthService();
    private SoapService soapService;
    private final XmlUtils xmlUtils = new XmlUtils();
    private JaxbService jaxbService;
    private final XmlRpcService xmlRpcService = new XmlRpcService();
    private SportService sportService;

    @FXML
    private Tab crudTab, soapTab, jaxbTab, xmlRpcTab, validationTab;

    @FXML
    private AnchorPane crudPane, soapPane, jaxbPane, xmlRpcPane, validationPane;

    @FXML
    private TextArea xsdInputArea, xsdResultArea, xsdSchemaArea, rngInputArea, rngResultArea, rngSchemaArea, soapResultArea, xmlRpcResultArea, jaxbResultArea, jaxbXmlArea, crudIdResultArea;

    @FXML
    private TextField soapTermField, xmlRpcCityField, crudNameField, crudSlugField;

    @FXML
    private Label crudStatusLabel;

    @FXML
    private TableView<SportDto> crudTable;

    @FXML
    private TableColumn<SportDto, Long> idColumn;

    @FXML
    private TableColumn<SportDto, String> nameColumn, slugColumn;

    @FXML
    private ComboBox<Long> crudIdCombo;

    private final ObservableList<SportDto> sports = FXCollections.observableArrayList();

    public void initialize() {
        xsdSchemaArea.setText(loadResourceAsString("schemes/sport.xsd"));
        rngSchemaArea.setText(loadResourceAsString("schemes/sport.rng"));
        idColumn.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        slugColumn.setCellValueFactory(data -> data.getValue().slugProperty());
        crudTable.setItems(sports);

        crudTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                crudNameField.setText(newSelection.getName());
                crudSlugField.setText(newSelection.getSlug());
            }
        });
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.soapService = new SoapService(authService);
        this.jaxbService = new JaxbService(authService);
        this.sportService = new SportService(authService);
        loadSports();
    }

    private void loadSports() {
        new Thread(() -> {
            List<SportDto> list = sportService.getAll();
            Platform.runLater(() -> {
                sports.clear();
                sports.setAll(list);
                List<Long> ids = list.stream().map(SportDto::getId).toList();
                crudIdCombo.getItems().setAll(ids);

                if (!ids.isEmpty()) {
                    crudIdCombo.getSelectionModel().selectFirst();
                }

                crudStatusLabel.setText("Učitano " + list.size() + " sportova.");
            });
        }).start();
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
        String accessToken = authService.getValidAccessToken();
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
        String accessToken = authService.getValidAccessToken();
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

    @FXML
    public void onSoapSearch() {
        String accessToken = authService.getValidAccessToken();
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
        soapService.searchSoap(term)
                .thenAccept(result -> Platform.runLater(() -> {
                    soapResultArea.setText(xmlUtils.prettyFormatXml(result));
                    jaxbTab.setDisable(false);

                    loadSports();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> soapResultArea.setText("Greška: " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    public void onXmlRpcSearch() {
        String city = xmlRpcCityField.getText();
        if (city == null || city.isBlank()) {
            xmlRpcResultArea.setText("Unesite naziv grada za pretragu.");
            return;
        }

        xmlRpcResultArea.setText("Pretražujem...");
        new Thread(() -> {
            String result = xmlRpcService.getTemperatures(city);
            Platform.runLater(() -> xmlRpcResultArea.setText(result));
        }).start();
    }

    @FXML
    public void onJaxbValidateAndShowXml() {
        jaxbResultArea.setText("Validiram...");
        jaxbXmlArea.setText("");

        new Thread(() -> {
            try {
                Thread.sleep(1000);

                List<String> results = jaxbService.validateSportsXml();
                String validationBody = String.join("\n", results);
                Platform.runLater(() -> jaxbResultArea.setText(validationBody));

                if (validationBody.contains("XML je validan prema XSD shemi")) {
                    String xml = jaxbService.showSportsXml();
                    String prettyXml = xmlUtils.prettyFormatXml(xml);

                    Platform.runLater(() -> {
                        jaxbXmlArea.setText(prettyXml);
                        loadSports();
                        crudTab.setDisable(false);
                    });
                } else {
                    Platform.runLater(() -> jaxbXmlArea.setText(""));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    jaxbResultArea.setText("Greška: " + e.getMessage());
                    jaxbXmlArea.setText("");
                });
            }
        }).start();
    }

    @FXML
    public void onCrudAdd() {
        String name = crudNameField.getText();
        String slug = crudSlugField.getText();
        if (name == null || name.isBlank() || slug == null || slug.isBlank()) {
            crudStatusLabel.setText("Unesite naziv i slug.");
            return;
        }

        new Thread(() -> {
            sportService.add(name, slug);
            Platform.runLater(() -> {
                loadSports();
                crudNameField.clear();
                crudSlugField.clear();
                crudStatusLabel.setText("Dodan " + name + ":"+ slug + " sport.");
            });
        }).start();
    }

    @FXML
    public void onCrudUpdate() {
        SportDto selectedSport = crudTable.getSelectionModel().getSelectedItem();

        if (selectedSport == null) {
            crudStatusLabel.setText("Izaberite sport.");
            return;
        }

        String name = crudNameField.getText();
        String slug = crudSlugField.getText();
        if (name == null || name.isBlank() || slug == null || slug.isBlank()) {
            crudStatusLabel.setText("Unesite naziv i slug.");
        }
        new Thread(() -> {
            sportService.update(selectedSport.getId(), name, slug);
            Platform.runLater(() -> {
                crudNameField.clear();
                crudSlugField.clear();
                crudStatusLabel.setText("Updatean sport iz " + selectedSport.getName() + ":" + selectedSport.getSlug() + "u " + name + ":" + slug + ".");
                loadSports();
            });
        }).start();
    }

    @FXML
    public void onCrudDelete() {
        SportDto selectedSport = crudTable.getSelectionModel().getSelectedItem();

        if (selectedSport == null) {
            crudStatusLabel.setText("Izaberite sport.");
            return;
        }

        new Thread(() -> {
            sportService.delete(selectedSport.getId());
            Platform.runLater(() -> {
                crudStatusLabel.setText("Obrisan sport " + selectedSport.getName() + ":" + selectedSport.getSlug() + ".");
                loadSports();
            });
        }).start();
    }

    @FXML
    public void onCrudShowById() {
        Long id = crudIdCombo.getValue();

        if (id == null) {
            crudIdResultArea.setText("Odaberite ID sporta.");
            return;
        }

        crudIdResultArea.setText("Dohvaćam...");
        new Thread(() -> {
           SportDto sport = sportService.getById(id);
           String prikaz = "ID: " + sport.getId() + "\nNaziv: " + sport.getName() + "\nSlug: " + sport.getSlug();
           Platform.runLater(() -> crudIdResultArea.setText(prikaz));
        }).start();

    }
}
