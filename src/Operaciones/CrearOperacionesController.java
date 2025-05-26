package Operaciones;

import Tareas.AgregarFXML;
import Tareas.FilaDependencia;
import Tareas.FilaInstruccion;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CrearOperacionesController {

    @FXML
    private Button btn_AgregarDependencia;

    @FXML
    private Button btn_Asociar;

    @FXML
    private Button btn_DescartarDependencia;

    @FXML
    private Button btn_Guardar;

    @FXML
    private Button btn_Salir;

    @FXML
    private SplitMenuButton spm_Tareas;

    @FXML
    private TextArea ta_Precondiciones;

    @FXML
    private TextField tf_NombreEntrada;

    @FXML
    private TextField tf_Salidas;
    
    @FXML
    private TableView<FilaDependencia> tbv_Dependencias;

    @FXML
    private TableColumn<FilaDependencia, String> tbc_Dependencias;
    
     public String nombreDependencia;
    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tbc_Dependencias.setCellValueFactory(new PropertyValueFactory<>("dependencia"));
        tbv_Dependencias.setItems(filasDependencia);
        AgregarFXML agregarTareasMenu = new AgregarFXML();
        agregarTareasMenu.cargarTareasEnMenu(spm_Tareas, this::SeleccionarMenuItem);
        MenuItem primerItem = spm_Tareas.getItems().get(0);
        String textoMenuItem = primerItem.getText();

        if (textoMenuItem.equals("No hay tareas creadas")) {
            btn_AgregarDependencia.setDisable(true);
            btn_DescartarDependencia.setDisable(true);
        }
    }

    @FXML
    private void SeleccionarMenuItem(ActionEvent event) {
        MenuItem fuente = (MenuItem) event.getSource();
        MenuItem selectedMenuItem = (MenuItem) event.getSource();
        String texto = selectedMenuItem.getText();
        spm_Tareas.setText(texto);
    }

    @FXML
    void AgregarDependencia(ActionEvent event) {
        nombreDependencia = spm_Tareas.getText();
        if (!nombreDependencia.isEmpty() && !nombreDependencia.equals("Tareas")) {
            FilaDependencia nueva = new FilaDependencia(nombreDependencia);
            filasDependencia.add(nueva);
            spm_Tareas.setText("Tareas");
            btn_AgregarDependencia.setDisable(true);
            btn_DescartarDependencia.setDisable(true);
        }
    }


    @FXML
    void DescartarDependencia(ActionEvent event) {
        FilaDependencia seleccionado = tbv_Dependencias.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            filasDependencia.remove(seleccionado);
        } else {
            System.out.println("No hay ningún elemento seleccionado para eliminar.");
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        
    }

    @FXML
    void Salir(ActionEvent event) {
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
     stage.close();
    }
}
