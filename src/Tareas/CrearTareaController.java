package Tareas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import Tareas.ConsultasSQL;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class CrearTareaController {

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
    private TableView<FilaInstruccion> tbv_Instrucciones;

    @FXML
    private TableView<FilaDependencia> tbv_Dependencias;

    @FXML
    private TableColumn<FilaInstruccion, String> tbc_Instrucciones;

    @FXML
    private TableColumn<FilaDependencia, String> tbc_Dependencias;

    @FXML
    private TextField tf_Descripcion;

    @FXML
    private TextField tf_NombreInstruccion;

    @FXML
    private TextField tf_NombreTarea;

    public String nombreInstruccion;
    public String nombreDependencia;
    private ObservableList<FilaInstruccion> filasInstruccion = FXCollections.observableArrayList();
    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();
    private ArrayList<String> ListaTareas;

    @FXML
    public void initialize() {
        tbc_Instrucciones.setCellValueFactory(new PropertyValueFactory<>("instruccion"));
        tbv_Instrucciones.setItems(filasInstruccion);
        tbc_Dependencias.setCellValueFactory(new PropertyValueFactory<>("dependencia"));
        tbv_Dependencias.setItems(filasDependencia);

        AgregarFXML.cargarTareasEnMenu(spm_Tareas, this::SeleccionarMenuItem);
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
    void AgregarInstruccion(ActionEvent event) {
        nombreInstruccion = tf_NombreInstruccion.getText();
        if (!nombreInstruccion.isEmpty()) {
            FilaInstruccion nueva = new FilaInstruccion(nombreInstruccion); // Solo instrucción
            filasInstruccion.add(nueva);
            tf_NombreInstruccion.clear();
        }
    }

    @FXML
    void AgregarDependencia(ActionEvent event) {
        nombreDependencia = spm_Tareas.getText();
        if (!nombreDependencia.isEmpty() && !nombreDependencia.equals("Tareas")) {
            FilaDependencia nueva = new FilaDependencia(nombreDependencia);
            filasDependencia.add(nueva);
            tf_NombreInstruccion.clear();
        }
    }

    @FXML
    void Descartar(ActionEvent event) {
        tf_NombreTarea.setText(null);
        tf_Descripcion.setText(null);
        tf_NombreInstruccion.setText(null);
        filasInstruccion.clear();
        filasDependencia.clear();
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
    void DescartarInstruccion(ActionEvent event) {
        FilaInstruccion seleccionado = tbv_Instrucciones.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            filasInstruccion.remove(seleccionado);
        } else {
            System.out.println("No hay ningún elemento seleccionado para eliminar.");
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
         Tareas tarea=null;
        String nombreTarea = tf_NombreTarea.getText();
        String descripcion = tf_Descripcion.getText();
        Boolean valorPausa = cb_Pausa.isSelected();
        Boolean valorReanudar = cb_Reanudar.isSelected();
        Boolean valorReiniciar = cb_Reiniciar.isSelected();

        String cadenaInstrucciones = filasInstruccion.stream()
                .map(fila -> fila.getInstruccion())
                .collect(Collectors.joining(","));

        String cadenaDependencias = filasDependencia.stream()
                .map(fila -> fila.getDependencia())
                .collect(Collectors.joining(","));
        if (validarCampos(nombreTarea, descripcion, cadenaInstrucciones, cadenaDependencias)) {
            tarea = new Tareas(nombreTarea, descripcion, valorPausa, valorReanudar, valorReiniciar, cadenaDependencias, cadenaInstrucciones);
        }
         ConsultasSQL crearTarea = new ConsultasSQL();
        Boolean creado = crearTarea.Guardar(tarea);
        if (creado) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Operación exitosa");
            alerta.setHeaderText(null);
            alerta.setContentText("Los datos se han guardado correctamente.");
        } else {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pudieron guardar los datos.");
        }

    }

    @FXML
    void Pausa(ActionEvent event) {

        if (cb_Pausa.isSelected()) {
            cb_Reanudar.setDisable(false);
            cb_Reiniciar.setDisable(false);
        } else {
            cb_Reanudar.setSelected(false);
            cb_Reiniciar.setSelected(false);
            cb_Reanudar.setDisable(true);
            cb_Reiniciar.setDisable(true);
        }
    }

    public boolean validarCampos(String nombreTarea, String descripcion,
            String cadenaInstrucciones, String cadenaDependencias) {
        if (nombreTarea == null || nombreTarea.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "El nombre de la tarea es obligatorio.");
            return false;
        }

        if (descripcion == null || descripcion.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "La descripción es obligatoria.");
            return false;
        }

        if (cadenaInstrucciones == null || cadenaInstrucciones.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "Debe haber al menos una instrucción.");
            return false;
        }

        if (cadenaDependencias == null || cadenaDependencias.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "Debe haber al menos una dependencia.");
            return false;
        }

        // Si llegamos aquí, todos los campos son válidos
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
