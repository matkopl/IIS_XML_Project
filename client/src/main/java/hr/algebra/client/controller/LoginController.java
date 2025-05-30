package hr.algebra.client.controller;

import hr.algebra.client.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final AuthService authService = new AuthService();

    @FXML
    protected void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        new Thread(() -> {
           boolean success = authService.login(username, password);

           Platform.runLater(() -> {
               if (success) {
                   statusLabel.setText("Uspješna prijava!");
                   try {
                       FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/algebra/client/MainView.fxml"));
                       Scene mainScene = new Scene(loader.load());
                       MainController controller = loader.getController();
                       controller.setAuthService(authService);
                       Stage stage = (Stage) statusLabel.getScene().getWindow();
                       stage.setScene(mainScene);
                       stage.setTitle("Sportski sustav");
                       stage.show();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               } else {
                   statusLabel.setText("Neuspješna prijava!");
               }
           });
        }).start();
    }


}
