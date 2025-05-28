package controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import mx.edu.uacm.is.slt.ds.gestor.App;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
