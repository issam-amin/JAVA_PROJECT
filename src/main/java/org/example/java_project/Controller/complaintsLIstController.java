package org.example.java_project.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class complaintsLIstController {

    String Type;



    @FXML
    void initialize() {

    }

    void getinfo(String info){
       Type=info;
    }
}
