package Controladores;

import ConsultasSQL.ConsultasTareas;
import Objetos.FilaInstruccion;
import Objetos.FilaDependencia;
import Objetos.FilaTareas;
import Tareas.AgregarFXML;
import Tareas.Tareas;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class VisorTareasController {

    @FXML
    private Button btn_AgregarDependencia;
    @FXML
    private Button btn_AgregarInstruccion;
    @FXML
    private Button btn_Salir;
    @FXML
    private Button btn_DescartarDependencia;
    @FXML
    private Button btn_DescartarInstruccion;
    @FXML
    private Button btn_Guardar;
    @FXML
    private Button btn_Eliminar;
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
    private TableView<FilaTareas> tbv_Tareas;
    @FXML
    private TableColumn<FilaTareas, String> tbc_Tareas;
    @FXML
    private TextField tf_Descripcion;
    @FXML
    private TextField tf_NombreInstruccion;
    @FXML
    private TextField tf_NombreTarea;
    @FXML
    private TextField tf_idTarea;

    public String nombreInstruccion;
    public String nombreDependencia;
    private ObservableList<FilaInstruccion> filasInstruccion = FXCollections.observableArrayList();
    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();
    private ObservableList<FilaTareas> filasTarea = FXCollections.observableArrayList();
    private ArrayList<String> ListaTareas;

    @FXML
    public void initialize() {
        tbc_Instrucciones.setCellValueFactory(new PropertyValueFactory<>("instruccion"));
        tbv_Instrucciones.setItems(filasInstruccion);
        tbc_Dependencias.setCellValueFactory(new PropertyValueFactory<>("dependencia"));
        tbv_Dependencias.setItems(filasDependencia);
        tbc_Tareas.setCellValueFactory(new PropertyValueFactory<>("tarea"));
        tbv_Tareas.setItems(filasTarea);

        cargarTareasEnTabla();

        tbv_Tareas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDatos(newSelection);
                setControlesHabilitados(true);
            }
        });

        AgregarFXML.cargarTareasEnMenu(spm_Tareas, this::SeleccionarMenuItem);
        MenuItem primerItem = spm_Tareas.getItems().get(0);
        String textoMenuItem = primerItem.getText();

        if (textoMenuItem.equals("No hay tareas creadas")) {
            btn_AgregarDependencia.setDisable(true);
            btn_DescartarDependencia.setDisable(true);
        }
        setControlesHabilitados(false);
    }

    @FXML
    private void SeleccionarMenuItem(ActionEvent event) {
        MenuItem selectedMenuItem = (MenuItem) event.getSource();
        String texto = selectedMenuItem.getText();
        spm_Tareas.setText(texto);
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
        String nombreTareaActual = tf_NombreTarea.getText();

        if (nombreDependencia.isEmpty() || nombreDependencia.equals("Tareas")) {
            return;
        }

        if (nombreDependencia.equals(nombreTareaActual)) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Dependencia inválida",
                    "No puedes agregar la misma tarea como su propia dependencia."
            );
            return;
        }

        FilaDependencia nueva = new FilaDependencia(nombreDependencia);
        filasDependencia.add(nueva);
        tf_NombreInstruccion.clear();
    }

    @FXML
    void Descartar(ActionEvent event) {
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }

    @FXML
    void DescartarDependencia(ActionEvent event) {
        FilaDependencia seleccionado = tbv_Dependencias.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            filasDependencia.remove(seleccionado);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No hay ningún elemento seleccionado");
        }
    }

    @FXML
    void DescartarInstruccion(ActionEvent event) {
        FilaInstruccion seleccionado = tbv_Instrucciones.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            filasInstruccion.remove(seleccionado);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No hay ningún elemento seleccionado");
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        Tareas tarea = null;
        AgregarFXML validar = new AgregarFXML();
        String textoId = tf_idTarea.getText();

        if (textoId == null || textoId.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No hay tarea seleccionada");
            return;
        }

        int idTarea = Integer.parseInt(textoId);
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

        boolean validacion = validar.validarCampos(nombreTarea, descripcion, cadenaInstrucciones);

        if (validacion) {
            tarea = new Tareas(nombreTarea, descripcion, valorPausa, valorReanudar, valorReiniciar, cadenaDependencias, cadenaInstrucciones);
        }

        ConsultasTareas crearTarea = new ConsultasTareas();
        Boolean creado = crearTarea.Modificar(tarea, idTarea);

        if (creado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa", "Los datos se han guardado correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron guardar los datos.");
        }
        PantallaPrincipalController.getInstancia().refrescarComponentesVisuales();

    }

    @FXML
    void Eliminar(ActionEvent event) {
        String textoId = tf_idTarea.getText();

        if (textoId == null || textoId.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron eliminar los datos.");
            return;
        }

        int id_Tarea = Integer.parseInt(textoId);

        ConsultasTareas crearTarea = new ConsultasTareas();
        Boolean creado = crearTarea.Eliminar(id_Tarea);

        if (creado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa", "Los datos se han eliminado correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron eliminar los datos.");
        }
        PantallaPrincipalController.getInstancia().refrescarComponentesVisuales();

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

    private void cargarTareasEnTabla() {
        filasTarea.clear();

        ConsultasTareas consultas = new ConsultasTareas();
        ArrayList<String> listaTareas = consultas.ListaTareas();

        if (listaTareas != null && !listaTareas.isEmpty()) {
            for (String tarea : listaTareas) {
                filasTarea.add(new FilaTareas(tarea));
            }
        } else {
            filasTarea.add(new FilaTareas("No existen tareas actualmente"));
        }
    }

    private void cargarDatos(FilaTareas tareaSeleccionada) {
        String comparar = tareaSeleccionada.getTarea();

        if (!comparar.equals("No existen tareas actualmente") && !comparar.equals("")) {
            ConsultasTareas consultaDetalleTarea = new ConsultasTareas();
            List<Tareas> listaTareas = consultaDetalleTarea.ConsultaTareas();

            Tareas tareaConsultada = null;
            for (Tareas t : listaTareas) {
                if (t.getNombreTarea().equals(comparar)) {
                    tareaConsultada = t;
                    break;
                }
            }

            if (tareaConsultada != null) {
                tf_NombreTarea.setText(tareaConsultada.getNombreTarea());
                tf_idTarea.setText(String.valueOf(tareaConsultada.getIdTarea()));

                filasInstruccion.clear();
                filasDependencia.clear();

                String instruccionesConsulta = tareaConsultada.getInstruccion();
                if (instruccionesConsulta != null && !instruccionesConsulta.trim().isEmpty()) {
                    for (String parte : instruccionesConsulta.split(",")) {
                        filasInstruccion.add(new FilaInstruccion(parte.trim()));
                    }
                }

                String dependenciaConsulta = tareaConsultada.getDependencia();
                if (dependenciaConsulta != null && !dependenciaConsulta.trim().isEmpty()) {
                    for (String parte : dependenciaConsulta.split(",")) {
                        filasDependencia.add(new FilaDependencia(parte.trim()));
                    }
                }

                cb_Pausa.setSelected(tareaConsultada.getValorPausa());
                cb_Reanudar.setSelected(tareaConsultada.getValorReanudar());
                cb_Reiniciar.setSelected(tareaConsultada.getValorReiniciar());
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "La tarea seleccionada no existe en la base de datos.");
            }
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No hay tareas creadas");
        }
    }

    private void setControlesHabilitados(boolean habilitado) {
        btn_AgregarDependencia.setDisable(!habilitado);
        btn_AgregarInstruccion.setDisable(!habilitado);
        btn_DescartarDependencia.setDisable(!habilitado);
        btn_DescartarInstruccion.setDisable(!habilitado);
        btn_Guardar.setDisable(!habilitado);
        btn_Eliminar.setDisable(!habilitado);
        cb_Pausa.setDisable(!habilitado);
        spm_Tareas.setDisable(!habilitado);
        tf_Descripcion.setDisable(!habilitado);
        tf_NombreInstruccion.setDisable(!habilitado);
        tf_NombreTarea.setDisable(!habilitado);
        tf_idTarea.setDisable(!habilitado);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
