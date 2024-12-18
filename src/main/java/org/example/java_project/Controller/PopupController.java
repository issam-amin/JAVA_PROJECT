package org.example.java_project.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.java_project.Service.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PopupController {

    @FXML
    private TextFlow messageLabel;
    private String idClient;
    private String currentStatus;
    private String client;
    private String recText;
    private String dateReclamation;

    @FXML
    private Button closeButton;
    @FXML
    private ComboBox<String> statusComboBox;

    public void setMessage(String client, String recText, String dateReclamation, String status, String idClient) {
        this.idClient = idClient;
        this.client = client;
        this.recText = recText;
        this.dateReclamation = dateReclamation;
        this.currentStatus = status;

        if (messageLabel == null) {
            System.out.println("messageLabel is null!");
        } else {
            refreshMessageLabel();
        }

        statusComboBox.setValue(status);
    }

    private void refreshMessageLabel() {
        messageLabel.getChildren().clear(); // Clear any existing text

        // Add formatted text content to messageLabel
        messageLabel.getChildren().addAll(
                createStyledText("ID:\t", idClient + "\t"),
                createStyledText("Client Name:\t", client + "\t"),
                createStyledText("Description:\t", recText  + "\t"),
                createStyledText("Date:\t", dateReclamation + "\t"),
                createStyledText("Status:\t", currentStatus)
        );
    }

    private TextFlow createStyledText(String title, String value) {
        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-weight: bold; -fx-fill: #0048ff; -fx-font-size: 18px;");

        Text valueText = new Text(value + "\n");
        valueText.setStyle("-fx-fill: #000000; -fx-font-family: 'Times New Roman'; -fx-font-size: 16px;");
        valueText.setWrappingWidth(580);  // Set wrapping width for valueText

        return new TextFlow(titleText, valueText);
    }

    @FXML
    private void updateStatus(ActionEvent actionEvent) {
        // Update the status based on the selection from ComboBox
        currentStatus = statusComboBox.getValue();
        updateStatusInDatabase(idClient, recText, dateReclamation, currentStatus);
        refreshMessageLabel();  // Refresh the message label after updating the status
    }

    private void updateStatusInDatabase(String idClient, String recText, String dateReclamation, String newStatus) {
        // Get database connection
        Connection connection = DbConnection.getConnection();

        // Updated SQL query with date_realisation column
        String updateQuery = "UPDATE rec SET status_Rec = ?, date_realisation = CURRENT_DATE WHERE id_C = ? AND rec_Text = ? AND date_Reclamation = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, newStatus);
            preparedStatement.setString(2, idClient);
            preparedStatement.setString(3, recText);
            preparedStatement.setString(4, dateReclamation);
            preparedStatement.executeUpdate();  // Execute the update

        } catch (SQLException e) {
            e.printStackTrace();  // Log SQL errors
            // Optionally handle database connection or update errors (e.g., show alert)
        }
    }


    @FXML
    private void closePopup(ActionEvent actionEvent) {
        updateStatus(actionEvent);  // Update status before closing
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();  // Close the current popup window
    }
}
