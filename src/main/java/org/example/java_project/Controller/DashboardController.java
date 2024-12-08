package org.example.java_project.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.example.java_project.Service.JobType;
import org.example.java_project.Service.RectypeJob;
import org.example.java_project.Service.UniqueUserCount;
import org.example.java_project.Service.clientTypeJob;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import static org.example.java_project.Controller.LoginController.loggedInUser;

public class DashboardController implements Initializable {
    @FXML
    private VBox view;
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
    private Button btnSettings;

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
        /*if (loggedInUser != null) {

        }*/
        initializeView();
    }

    private void initializeView() {
        view.getChildren().clear();
        try {
            view.setStyle("-fx-background-color: #02030A");
            view.toFront();
            Node node = FXMLLoader.load(getClass().getResource("../Overview.fxml"));
            view.getChildren().add(node);
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
                top3job.getChildren().add(node);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        @FXML
        void PieChart() {

            typeIssue.getChildren().clear();
            try {
                typeIssue.setStyle("-fx-background-color : #53639F");
                typeIssue.toFront();
                Node RecType = FXMLLoader.load(getClass().getResource("../PieChart.fxml"));
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
        view.getChildren().clear();
        try {
            view.setStyle("-fx-background-color : #02030A");
            view.toFront();
            Node node = FXMLLoader.load(getClass().getResource("../Overview.fxml"));
            view.getChildren().add(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    void ClientTypeCount() {
        typeIssue.getChildren().clear();
        try {
            typeIssue.setStyle("-fx-background-color : #53639F");
            typeIssue.toFront();
            Node node = FXMLLoader.load(getClass().getResource("../ClientTypeCount.fxml"));
            typeIssue.getChildren().add(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @FXML
    void UniqueUserCount() {
        typeIssue.getChildren().clear();
        try {
            typeIssue.setStyle("-fx-background-color : #53639F");
            typeIssue.toFront();
            Node node = FXMLLoader.load(getClass().getResource("../UniqueUserCount.fxml"));
            typeIssue.getChildren().add(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




}