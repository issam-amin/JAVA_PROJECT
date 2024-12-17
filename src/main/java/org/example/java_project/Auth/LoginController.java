package org.example.java_project.Auth;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.gluonhq.charm.glisten.control.ProgressIndicator;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.java_project.Service.DbConnection;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class LoginController{
    @FXML
    public Button loginButton;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private HBox label_progress;


    public boolean CheckCredentials(String username, String password)  {
        try{

        Connection connection =  DbConnection.getConnection();
        PreparedStatement query=  connection.prepareStatement("select * from Admin where email = ? and password = ?");
        query.setString(1, username);
        query.setString(2, password);
        ResultSet res =  query.executeQuery();
        res.next() ;
        if(res.getString("password").equals(password) && res.getString("email").equals(username)){
            SessionManager.getInstance().setUserId(res.getInt(1));
            return true;
        }

        System.out.println(res.next());
        }catch (SQLException e) {
            System.out.println("db connection error");
            return false;
        }

        return false;
    }
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Simulated authentication

        Task<Void> login = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (CheckCredentials(username,password)) {
                    // Set session data
                    SessionManager session = SessionManager.getInstance();
                    session.setUsername(username);

                    Platform.runLater(()->{
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("../Home.fxml"));
                            System.out.println(loader.getLocation());
                            Parent root = loader.load();
                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setFullScreen(true);
                            stage.centerOnScreen();
                            stage.show();

                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException();
                        }

                        System.out.println("logged in seccessfull");
                    });

                } else {
                    System.out.println("username or password is incorrect");
                    throw new RuntimeException();
                }
                return null;
            }
        };


        login.setOnRunning(TaskEvent->{
            ProgressIndicator progressIndicator = new ProgressIndicator();
            label_progress.getChildren().clear();
            label_progress.getChildren().add(progressIndicator);
        });

        login.setOnFailed(TaskEvent->{
            Label error = new Label();
            label_progress.getChildren().clear();
            error.setText("username or password is incorrect");
            error.setStyle("-fx-text-fill: red");
            label_progress.getChildren().add(error);
        });

        Thread myLoginThread = new Thread(login);
        myLoginThread.start();



    }





}
