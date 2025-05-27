package Tareas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.util.ArrayList;
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
        spm_Tareas.setText(fuente.getText());
    }

    @FXML
    void AgregarInstruccion(ActionEvent event) {
        nombreInstruccion = tf_NombreInstruccion.getText();
        if (!nombreInstruccion.isEmpty()) {
            FilaInstruccion nueva = new FilaInstruccion(nombreInstruccion);
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
            spm_Tareas.setText("Tareas");
            btn_AgregarDependencia.setDisable(true);
            btn_DescartarDependencia.setDisable(true);
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
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("No hay ningún elemento seleccionado.");
            alerta.showAndWait();
        }
    }

    @FXML
    void DescartarInstruccion(ActionEvent event) {
        FilaInstruccion seleccionado = tbv_Instrucciones.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            filasInstruccion.remove(seleccionado);
        } else {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("No hay ningún elemento seleccionado.");
            alerta.showAndWait();
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        Tareas tarea = null;
        AgregarFXML validar = new AgregarFXML();

        String nombreTarea = tf_NombreTarea.getText();
        String descripcion = tf_Descripcion.getText();
        Boolean valorPausa = cb_Pausa.isSelected();
        Boolean valorReanudar = cb_Reanudar.isSelected();
        Boolean valorReiniciar = cb_Reiniciar.isSelected();

        String cadenaInstrucciones = filasInstruccion.stream()
                .map(FilaInstruccion::getInstruccion)
                .collect(Collectors.joining(","));

        String cadenaDependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        boolean validacion= validar.validarCampos(nombreTarea, descripcion, cadenaInstrucciones);

        if (validacion) {
            tarea = new Tareas(nombreTarea, descripcion, valorPausa, valorReanudar, valorReiniciar, cadenaDependencias, cadenaInstrucciones);
            ConsultasSQL crearTarea = new ConsultasSQL();
            boolean creado = crearTarea.Guardar(tarea);

            Alert alerta = new Alert(creado ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alerta.setTitle(creado ? "Operación exitosa" : "Error");
            alerta.setHeaderText(null);
            alerta.setContentText(creado ? "Los datos se han guardado correctamente." : "No se pudieron guardar los datos.");
            alerta.showAndWait();
        }
    }

    @FXML
    void Pausa(ActionEvent event) {
        boolean seleccionada = cb_Pausa.isSelected();
        cb_Reanudar.setDisable(!seleccionada);
        cb_Reiniciar.setDisable(!seleccionada);
        if (!seleccionada) {
            cb_Reanudar.setSelected(false);
            cb_Reiniciar.setSelected(false);
        }
    }

    @FXML
    void Salir(ActionEvent event) {
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }
}
