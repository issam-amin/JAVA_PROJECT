package org.example.java_project.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.java_project.Auth.LogoutController;
import org.example.java_project.Service.JobType;
import org.example.java_project.Service.RectypeJob;
import org.example.java_project.Service.UniqueUserCount;
import org.example.java_project.Service.clientTypeJob;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;



public class DashboardController implements Initializable {

    @FXML
    private VBox overview;
    @FXML
    private VBox typeIssue;
    @FXML
    private VBox pnItems = null;
    @FXML
    private Button btnOverview;
    @FXML
    private VBox top3job ;

    @FXML
    private Button btnOrders;

    @FXML
    private Button btnCustomers;

    @FXML
    private Button btnMenus;

    @FXML
    private Button btnPackages;

    @FXML
    private Button btnPrediction;

    @FXML
    private Button btnSignout;

    @FXML
    private Pane pnlCustomer;

    @FXML
    private Pane pnlOrders;

    @FXML
    private Pane pnlOverview;

    @FXML
    private Pane pnlMenus;
    private String loggedInUser;


    @FXML
    public void setLoggedInUser(String username) {
        this.loggedInUser = username;
        initializeView();  // Call the method to initialize the view with the user data
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeView();
    }

    private void initializeView() {
        overview.getChildren().clear();
        try {
            overview.setStyle("-fx-background-color: #02030A");
            overview.toFront();
            Node node = FXMLLoader.load(getClass().getResource("../Overview.fxml"));
            VBox.setVgrow(node, Priority.ALWAYS);
            overview.getChildren().add(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



        @FXML
        public void handleTop3() {
            top3job.getChildren().clear();
            try {
                top3job.setStyle("-fx-background-color : #02030A");
                top3job.toFront();
                Node node = FXMLLoader.load(getClass().getResource("../Barchar/Top3job.fxml"));
                VBox.setVgrow(node, Priority.ALWAYS);
                top3job.getChildren().add(node);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        @FXML
        void PieChart() {

            typeIssue.getChildren().clear();
            try {
                typeIssue.setStyle("-fx-background-color : #f6f6f6");
                typeIssue.toFront();
                Node RecType = FXMLLoader.load(getClass().getResource("../PieChart.fxml"));
                VBox.setVgrow(RecType, Priority.ALWAYS);
                typeIssue.getChildren().add(RecType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
     /*   @FXML
        void clientType() {
            typeIssue.getChildren().clear();
            try {
                typeIssue.setStyle("-fx-background-color : #53639F");
                typeIssue.toFront();
                Node RecType = FXMLLoader.load(getClass().getResource("../ClientTypeCountController.fxml"));
                typeIssue.getChildren().add(RecType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }*/

     @FXML
     void overview() {
      initializeView();
    }
    @FXML
    void ClientTypeCount() {
        typeIssue.getChildren().clear();
        try {
            typeIssue.setStyle("-fx-background-color : #e6e2e2");
            typeIssue.toFront();
            Node node = FXMLLoader.load(getClass().getResource("../ClientTypeCount.fxml"));
            VBox.setVgrow(node, Priority.ALWAYS);
            typeIssue.getChildren().add(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @FXML
    void UniqueUserCount() {
        typeIssue.getChildren().clear();
        try {
            typeIssue.setStyle("-fx-background-color : #f6f6f6");
            typeIssue.toFront();
            Node node = FXMLLoader.load(getClass().getResource("../UniqueUserCount.fxml"));
            VBox.setVgrow(node, Priority.ALWAYS);
            typeIssue.getChildren().add(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


        @FXML
        void SparkModel(ActionEvent actionEvent) {
            typeIssue.getChildren().clear();
            try {
                typeIssue.setStyle("-fx-background-color : #ffffff");
                typeIssue.toFront();
                Node node = FXMLLoader.load(getClass().getResource("../Predictions.fxml"));
                VBox.setVgrow(node, Priority.ALWAYS);
                typeIssue.getChildren().add(node);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        @FXML

        public void signOut()  {

            try {
                LogoutController.logout();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

}