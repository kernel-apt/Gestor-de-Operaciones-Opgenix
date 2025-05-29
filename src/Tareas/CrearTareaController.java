package Tareas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.stream.Collectors;

public class CrearTareaController {

    @FXML
    private Button btn_AgregarDependencia;
    @FXML
    private Button btn_AgregarInstruccion;
    @FXML
    private Button btn_Descartar;
    @FXML
    private Button btn_Salir;
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
    private TableView<FilaInstruccion> tbv_Instrucciones;
    @FXML
    private TableView<FilaDependencia> tbv_Dependencias;

    @FXML
    private TableColumn<FilaInstruccion, String> tbc_Instrucciones;
    @FXML
    private TableColumn<FilaDependencia, String> tbc_Dependencias;
    @FXML
    private ObservableList<FilaTareas> tareasObservable = FXCollections.observableArrayList();

    @FXML
    private TableView<FilaTareas> tbv_Tareas;

    @FXML
    private TextField tf_Descripcion;
    @FXML
    private TextField tf_NombreInstruccion;
    @FXML
    private TextField tf_NombreTarea;

    private ObservableList<FilaInstruccion> filasInstruccion = FXCollections.observableArrayList();
    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tbc_Instrucciones.setCellValueFactory(new PropertyValueFactory<>("instruccion"));
        tbv_Instrucciones.setItems(filasInstruccion);
        tbc_Dependencias.setCellValueFactory(new PropertyValueFactory<>("dependencia"));
        tbv_Dependencias.setItems(filasDependencia);

        AgregarFXML.cargarTareasEnMenu(spm_Tareas, this::SeleccionarMenuItem);
        if (spm_Tareas.getItems().get(0).getText().equals("No hay tareas creadas")) {
            btn_AgregarDependencia.setDisable(true);
            btn_DescartarDependencia.setDisable(true);
        }
    }

    @FXML
    private void SeleccionarMenuItem(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        spm_Tareas.setText(item.getText());
    }

    @FXML
    private void AgregarInstruccion(ActionEvent event) {
        String instruccion = tf_NombreInstruccion.getText();
        if (!instruccion.isEmpty()) {
            filasInstruccion.add(new FilaInstruccion(instruccion));
            tf_NombreInstruccion.clear();
        }
    }

    @FXML
    private void AgregarDependencia(ActionEvent event) {
        String dependencia = spm_Tareas.getText();
        if (!dependencia.isEmpty() && !dependencia.equals("Tareas")) {
            filasDependencia.add(new FilaDependencia(dependencia));
            spm_Tareas.setText("Tareas");
            btn_AgregarDependencia.setDisable(true);
            btn_DescartarDependencia.setDisable(true);
        }
    }

    @FXML
    private void Descartar(ActionEvent event) {
        limpiarFormulario();
    }

    @FXML
    private void DescartarDependencia(ActionEvent event) {
        FilaDependencia seleccionada = tbv_Dependencias.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            filasDependencia.remove(seleccionada);
        } else {
            mostrarAlerta("Error", "No hay ningún elemento seleccionado.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void DescartarInstruccion(ActionEvent event) {
        FilaInstruccion seleccionada = tbv_Instrucciones.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            filasInstruccion.remove(seleccionada);
        } else {
            mostrarAlerta("Error", "No hay ningún elemento seleccionado.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void Guardar(ActionEvent event) {
        AgregarFXML validador = new AgregarFXML();

        String nombreTarea = tf_NombreTarea.getText();
        String descripcion = tf_Descripcion.getText();
        boolean pausa = cb_Pausa.isSelected();
        boolean reanudar = cb_Reanudar.isSelected();
        boolean reiniciar = cb_Reiniciar.isSelected();

        String instrucciones = filasInstruccion.stream()
                .map(FilaInstruccion::getInstruccion)
                .collect(Collectors.joining(","));

        String dependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        if (!validador.validarCampos(nombreTarea, descripcion, instrucciones)) {
            return;
        }

        Tareas tarea = new Tareas(nombreTarea, descripcion, pausa, reanudar, reiniciar, dependencias, instrucciones);
        ConsultasSQL consulta = new ConsultasSQL();
        boolean exito = consulta.Guardar(tarea);

        mostrarAlerta(
                exito ? "Operación exitosa" : "Error",
                exito ? "Los datos se han guardado correctamente." : "No se pudieron guardar los datos.",
                exito ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );

        limpiarFormulario();
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void Pausa(ActionEvent event) {
        boolean activa = cb_Pausa.isSelected();
        cb_Reanudar.setDisable(!activa);
        cb_Reiniciar.setDisable(!activa);
        if (!activa) {
            cb_Reanudar.setSelected(false);
            cb_Reiniciar.setSelected(false);
        }
    }

    @FXML
    private void Salir(ActionEvent event) {
        limpiarFormulario();
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }

    private void limpiarFormulario() {
        tf_NombreTarea.clear();
        tf_Descripcion.clear();
        tf_NombreInstruccion.clear();
        filasInstruccion.clear();
        filasDependencia.clear();
        cb_Pausa.setSelected(false);
        cb_Reanudar.setSelected(false);
        cb_Reiniciar.setSelected(false);
        cb_Reanudar.setDisable(true);
        cb_Reiniciar.setDisable(true);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
