package Tareas;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class VisorTareasController {

    @FXML
    private Button btn_AgregarDependencia;

    @FXML
    private Button btn_AgregarInstruccion;

    @FXML
    private Button btn_Descartar;

    @FXML
    private Button btn_DescartarDependencia;

    @FXML
    private Button btn_DescartarInstruccion;

    @FXML
    private Button btn_Guardar;

    @FXML
    private CheckBox cb_Pausa;

    @FXML
    private CheckBox cb_Reanudar;

    @FXML
    private CheckBox cb_Reiniciar;

    @FXML
    private SplitMenuButton spm_Tareas;

    @FXML
    private TableColumn<?, ?> tbc_Dependencias;

    @FXML
    private TableColumn<?, ?> tbc_Instrucciones;

    @FXML
    private TableView<?> tbv_Instrucciones;

    @FXML
    private TextField tf_Descripcion;

    @FXML
    private TextField tf_NombreInstruccion;

    @FXML
    private TextField tf_NombreTarea;

    @FXML
    void AgregarDependencia(ActionEvent event) {

    }

    @FXML
    void AgregarInstruccion(ActionEvent event) {

    }

    @FXML
    void Descartar(ActionEvent event) {

    }

    @FXML
    void DescartarDependencia(ActionEvent event) {

    }

    @FXML
    void DescartarInstruccion(ActionEvent event) {

    }

    @FXML
    void Guardar(ActionEvent event) {

    }

}
