package org.example.java_project.Auth;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.example.java_project.MainD;

import java.io.IOException;

import static org.example.java_project.MainD.primaryStage;

public class LogoutController {
    private static double x, y;
    static  public  void  logout() throws IOException {
        SessionManager.getInstance().clearSession();
        Parent root = FXMLLoader.load(LogoutController.class.getResource("../LoginPage.fxml"));
        primaryStage.setScene(new Scene(root));

        primaryStage.setHeight(574);
        primaryStage.setWidth(884);

        // Set stage borderless
        // Add dragging functionality
        root.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - x);
            primaryStage.setY(event.getScreenY() - y);
        });
        primaryStage.show();
    }
}
