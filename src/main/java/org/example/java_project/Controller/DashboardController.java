package org.example.java_project.Controller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

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
    private VBox pnlOverview;

    @FXML
    private Pane pnlMenus;
    @FXML

    @Override
    public void initialize(URL location, ResourceBundle resources) {


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
            pnlMenus.getChildren().clear();
            try {
                pnlMenus.setStyle("-fx-background-color : #53639F");
                pnlMenus.toFront();
                Node RecType = FXMLLoader.load(getClass().getResource("../PieChart.fxml"));
                pnlMenus.getChildren().add(RecType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        @FXML
        void home (){
            /*pnlOverview.getChildren().clear();*/
       /*     try {*/
            pnlOverview.toFront();
              /*  Node node = FXMLLoader.load(getClass().getResource("../Barchar/Top3job.fxml"));
                pnlOverview.getChildren().add(node);*/
            /*} catch (IOException e) {
                throw new RuntimeException(e);
            }*/
        }

    }