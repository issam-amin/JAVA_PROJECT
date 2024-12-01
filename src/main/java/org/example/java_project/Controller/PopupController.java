package org.example.java_project.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PopupController {
    @FXML
    private Label messageLabel;

    // Set the message dynamically
    public void setMessage(String client, String recText, String dateReclamation) {
        messageLabel.setText("Client: " + client + "\nRec Text: " + recText + "\nDate: " + dateReclamation);
    }

    // Close the popup
    @FXML
    private void closePopup() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}
