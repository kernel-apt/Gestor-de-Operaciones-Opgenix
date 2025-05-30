/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controladores;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AcercaDeController {

    @FXML
    private Button btn_Regresar;

    @FXML
    void Regresar(ActionEvent event) {
        Stage stage = (Stage) btn_Regresar.getScene().getWindow();
        stage.close();
    }

}