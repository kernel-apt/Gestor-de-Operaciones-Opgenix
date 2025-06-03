package Controladores;

import ConsultasSQL.ConsultaInstrucciones;
import ConsultasSQL.ConsultasTareas;
import Objetos.FilaInstruccion;
import Objetos.FilaDependencia;
import Objetos.FilaTareas;
import Validaciones.AgregarTareas;
import Objetos.Tareas;
import java.sql.Connection;
import gestorDeOperaciones.GestorDeOperaciones;
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
    @FXML
    private TextField tf_SalidaEsperada;

    Connection con = GestorDeOperaciones.getConnection();

    private ObservableList<FilaInstruccion> filasInstruccion = FXCollections.observableArrayList();
    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tbc_Instrucciones.setCellValueFactory(new PropertyValueFactory<>("instruccion"));
        tbv_Instrucciones.setItems(filasInstruccion);
        tbc_Dependencias.setCellValueFactory(new PropertyValueFactory<>("dependencia"));
        tbv_Dependencias.setItems(filasDependencia);

        AgregarTareas.cargarTareasEnMenu(con, spm_Tareas, this::SeleccionarMenuItem);
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
    String instruccion = tf_NombreInstruccion.getText().trim();
    
    if (!instruccion.isEmpty()) {
        boolean yaExiste = filasInstruccion.stream()
                .anyMatch(f -> f.getInstruccion().equalsIgnoreCase(instruccion));

        if (yaExiste) {
            mostrarAlerta( "Instrucción duplicada", "La instrucción ya ha sido agregada.", Alert.AlertType.WARNING);
            return;
        }

        filasInstruccion.add(new FilaInstruccion(instruccion));
        tf_NombreInstruccion.clear();
    }
}


   @FXML
private void AgregarDependencia(ActionEvent event) {
    String dependencia = spm_Tareas.getText();
    if (!dependencia.isEmpty() && !dependencia.equals("Tareas")) {
        filasDependencia.add(new FilaDependencia(dependencia));
        spm_Tareas.getItems().removeIf(item -> item.getText().equals(dependencia));
        spm_Tareas.setText("Tareas");
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
        AgregarTareas validador = new AgregarTareas();

        String nombreTarea = tf_NombreTarea.getText().trim();
        String descripcion = tf_Descripcion.getText();
        String salida = tf_SalidaEsperada.getText();
        boolean pausa = cb_Pausa.isSelected();
        boolean reanudar = cb_Reanudar.isSelected();

        String instrucciones = filasInstruccion.stream()
                .map(FilaInstruccion::getInstruccion)
                .collect(Collectors.joining(","));

        String dependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        if (!validador.validarCampos(nombreTarea, descripcion, instrucciones, salida)) {
            return;
        }

        ConsultasTareas consulta = new ConsultasTareas(con);
        ConsultaInstrucciones consultaInstruccion = new ConsultaInstrucciones(con);

        Tareas tarea = new Tareas(nombreTarea, descripcion, pausa, reanudar, true, dependencias, salida);
        boolean exito = consulta.Guardar(tarea);

        if (!exito) {
            mostrarAlerta("Error", "No se pudo guardar la tarea.", Alert.AlertType.ERROR);
            return;
        }

        for (FilaInstruccion fila : filasInstruccion) {
            String nombreInstruccion = fila.getInstruccion().trim();
            boolean exitoInstruccion = consultaInstruccion.crearInstruccion(nombreInstruccion, nombreTarea);
            if (!exitoInstruccion) {
                mostrarAlerta("Error", "No se pudo guardar la instrucción: " + nombreInstruccion, Alert.AlertType.ERROR);
                return;
            }
        }

        mostrarAlerta("Éxito", "Los datos se han guardado correctamente.", Alert.AlertType.INFORMATION);
        limpiarFormulario();
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void Pausa(ActionEvent event) {
        boolean activa = cb_Pausa.isSelected();
        cb_Reanudar.setDisable(!activa);
        if (!activa) {
            cb_Reanudar.setSelected(false);
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
        cb_Reanudar.setDisable(true);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    
    
}
