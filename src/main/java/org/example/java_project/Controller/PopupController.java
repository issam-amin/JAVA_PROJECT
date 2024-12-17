package org.example.java_project.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.java_project.Service.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PopupController {
    @FXML
    private Label messageLabel;
    private String idClient;
    private String currentStatus;
    private String client;
    private String recText;
    private String dateReclamation;

    @FXML
    private ComboBox<String> statusComboBox;

    // Set the message dynamically
    public void setMessage(String client, String recText, String dateReclamation, String status, String idClient) {
        this.idClient = idClient;
        this.client = client;
        this.recText = recText;
        this.dateReclamation = dateReclamation;
        this.currentStatus = status;
        messageLabel.setText("ID: " + idClient + "\nClient: " + client + "\nRec Text: " + recText + "\nDate: " + dateReclamation + "\nStatus: " + status);
        statusComboBox.setValue(status);
    }

    @FXML
    private void updateStatus(ActionEvent actionEvent) {
        currentStatus = statusComboBox.getValue();
        String[] lines = messageLabel.getText().split("\n");
        lines[4] = "Status: " + currentStatus;
        messageLabel.setText(String.join("\n", lines));
        updateStatusInDatabase(idClient, recText, dateReclamation, currentStatus);
    }

    private void updateStatusInDatabase(String idClient, String recText, String dateReclamation, String newStatus) {
        Connection connection = DbConnection.getConnection();

        String updateQuery = "UPDATE rec SET status_Rec = ? WHERE id_C = ? AND rec_Text = ? AND date_Reclamation = ?"; // Replace with your table and column names

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, newStatus);
            preparedStatement.setString(2, idClient);
            preparedStatement.setString(3, recText);
            preparedStatement.setString(4, dateReclamation);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database connection or update errors
        }
    }

    @FXML
    private void closePopup() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }
}
