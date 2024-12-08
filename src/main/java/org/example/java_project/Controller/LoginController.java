package org.example.java_project.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import static org.example.java_project.Service.DbConnection.validateLogin;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private ProgressIndicator label_progress;

    @FXML
    private Text loginMessage;

    protected static String loggedInUser;

    @FXML
    private void handleLogin(ActionEvent event) {
        if (usernameField == null || passwordField == null || label_progress == null || loginMessage == null) {
            System.out.println("One of the required fields is null");
            return;
        }

        String username = usernameField.getText();
        String password = passwordField.getText();

        label_progress.setVisible(true);

        if (validateLogin(username, password)) {
            loggedInUser = username;
            loginMessage.setText("Login successful!");

            // Load the home.fxml file
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../Home.fxml"));
                Parent homeRoot = loader.load();

                DashboardController dashboardController = loader.getController();
                if (dashboardController != null) {
                    dashboardController.setLoggedInUser(loggedInUser);
                } else {
                    System.out.println("DashboardController is null");
                }

                // Get the current stage and set the new scene
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(homeRoot);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loginMessage.setText("Username or password is incorrect.");
        }

        label_progress.setVisible(false);

        System.out.println(username + " " + password);
    }

    public static String getLoggedInUser() {
        return loggedInUser;
    }
}
