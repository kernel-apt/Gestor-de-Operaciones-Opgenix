package Operaciones;

import Tareas.AgregarFXML;
import Tareas.ConsultasSQL;
import Tareas.FilaDependencia;
import Tareas.FilaTareas;
import Tareas.Tareas;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EditorOperacionesController {

    @FXML private Button btn_AgregarDependencia;
    @FXML private Button btn_DescartarDependencia;
    @FXML private Button btn_Eliminar;
    @FXML private Button btn_Guardar;
    @FXML private Button btn_Salir;
    @FXML private SplitMenuButton spm_Tareas;
    @FXML private TextArea ta_Precondiciones;
    @FXML private TableView<FilaDependencia> tbv_Dependencias;
    @FXML private TableColumn<FilaDependencia, String> tbc_Dependencias;
    @FXML private TableView<FilaOperacion> tbv_Operaciones;
    @FXML private TableColumn<FilaOperacion, String> tbc_Operaciones;
    @FXML private TextField tf_LimiteDeTareas;
    @FXML private TextField tf_NombreEntrada;
    @FXML private TextField tf_Salidas;
    @FXML private TextField tf_id;

    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();
    private ObservableList<FilaOperacion> filasOperaciones = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tbc_Dependencias.setCellValueFactory(new PropertyValueFactory<>("dependencia"));
        tbv_Dependencias.setItems(filasDependencia);
        tbc_Operaciones.setCellValueFactory(new PropertyValueFactory<>("operacion"));
        tbv_Operaciones.setItems(filasOperaciones);
        cargarOperacionesEnTabla();

        tbv_Operaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDatos(newSelection);
            }
        });

        Agregarfxml.cargarOperacionesEnMenu(spm_Tareas, this::SeleccionarMenuItem);
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
    void AgregarDependencia(ActionEvent event) {
        try {
            if (tf_LimiteDeTareas.getText().isEmpty()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Campo vacío", "Por favor, indica un límite de tareas.");
                return;
            }

            int numeroOperaciones = Integer.parseInt(tf_LimiteDeTareas.getText());
            String nombreDependencia = spm_Tareas.getText().trim();

            if (nombreDependencia.isEmpty() || nombreDependencia.equals("Tareas")) {
                return;
            }

            agregarDependenciaSiValida(nombreDependencia, numeroOperaciones);
            spm_Tareas.setText("Tareas");

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de entrada", "No se ha indicado un límite de tareas correcto.");
        }
    }

    @FXML
    void DescartarDependencia(ActionEvent event) {
        FilaDependencia seleccionado = tbv_Dependencias.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            filasDependencia.remove(seleccionado);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de entrada", "No hay ningún elemento seleccionado para eliminar.");
        }
    }

    @FXML
    void Eliminar(ActionEvent event) {
        Agregarfxml utilidades = new Agregarfxml();
        String textoId = tf_id.getText();
        if (textoId == null || textoId.trim().isEmpty()) {
            utilidades.mostrarAlerta("Entrada inválida", "El campo ID no puede estar vacío.");
            return;
        }
        int idOperacion = Integer.parseInt(tf_id.getText());
        Consultassql crearTarea = new Consultassql();
        Boolean creado = crearTarea.Eliminar(idOperacion);

        if (creado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa", "Los datos se han eliminado correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron eliminar los datos.");
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        Agregarfxml utilidades = new Agregarfxml();
        String nombreOperacion = tf_NombreEntrada.getText();
        String textoId = tf_id.getText();

        if (textoId == null || textoId.trim().isEmpty()) {
            utilidades.mostrarAlerta("Entrada inválida", "El campo ID no puede estar vacío.");
            return;
        }

        int idOperacion;
        try {
            idOperacion = Integer.parseInt(textoId);
        } catch (NumberFormatException e) {
            utilidades.mostrarAlerta("Entrada inválida", "El ID debe ser un número.");
            return;
        }

        int numeroOperaciones;
        try {
            numeroOperaciones = Integer.parseInt(tf_LimiteDeTareas.getText());
        } catch (NumberFormatException e) {
            utilidades.mostrarAlerta("Entrada inválida", "No se ha indicado un límite de tareas correcto.");
            return;
        }

        String cadenaDependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        if (!utilidades.validarDatos(nombreOperacion.trim(), numeroOperaciones, cadenaDependencias)) {
            return;
        }

        Operacion operacion = new Operacion(nombreOperacion.trim(), numeroOperaciones, cadenaDependencias);
        Consultassql consultas = new Consultassql();
        boolean creado = consultas.Modificar(operacion, idOperacion);

        if (creado) {
            utilidades.mostrarAlerta("Operación exitosa", "Los datos se han guardado correctamente.");
        } else {
            utilidades.mostrarAlerta("Error", "No se pudieron guardar los datos.");
        }
    }

    @FXML
    void Salir(ActionEvent event) {
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }

    private void cargarOperacionesEnTabla() {
        filasOperaciones.clear();
        Consultassql consultas = new Consultassql();
        ArrayList<String> listaOperacion = consultas.ListaOperaciones();

        if (listaOperacion != null && !listaOperacion.isEmpty()) {
            for (String operacion : listaOperacion) {
                filasOperaciones.add(new FilaOperacion(operacion));
            }
        } else {
            filasOperaciones.add(new FilaOperacion("No existen operaciones actualmente"));
        }
    }

    private void cargarDatos(FilaOperacion operacionSeleccionada) {
        String comparar = operacionSeleccionada.getOperacion();

        if (!comparar.equals("No existen operaciones actualmente") && !comparar.equals("")) {
            Consultassql consultaDetalleOperacion = new Consultassql();
            List<Operacion> operacionesConsultadas = consultaDetalleOperacion.ConsultaOperacion();
            Operacion operacionConsultada = null;
            for (Operacion op : operacionesConsultadas) {
                if (op.getNombreOperacion().equals(comparar)) {
                    operacionConsultada = op;
                    break;
                }
            }

            if (operacionConsultada != null) {
                tf_NombreEntrada.setText(operacionConsultada.getNombreOperacion());
                tf_id.setText(String.valueOf(operacionConsultada.getId()));
                filasDependencia.clear();

                String dependenciaConsulta = operacionConsultada.getTareasAsociadas();
                if (dependenciaConsulta != null && !dependenciaConsulta.trim().isEmpty()) {
                    String[] dependencias = dependenciaConsulta.split(",");
                    for (String parte : dependencias) {
                        filasDependencia.add(new FilaDependencia(parte.trim()));
                    }
                }
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "La operación seleccionada no existe en la base de datos.");
            }
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No hay operaciones creadas");
        }
    }

    private void agregarDependenciaSiValida(String nombreDependencia, int limite) {
        if (filasDependencia.stream().anyMatch(f -> f.getDependencia().equals(nombreDependencia))) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dependencia repetida", "Esta tarea ya fue agregada como dependencia.");
            return;
        }

        if (filasDependencia.size() >= limite) {
            mostrarAlerta(Alert.AlertType.ERROR, "Límite alcanzado", "Ha alcanzado el límite de tareas para esta operación.");
            return;
        }

        FilaDependencia nueva = new FilaDependencia(nombreDependencia);
        filasDependencia.add(nueva);

        ConsultasSQL consulta = new ConsultasSQL();
        List<Tareas> tareas = consulta.ConsultaTareas(nombreDependencia);
        if (tareas != null && !tareas.isEmpty()) {
            String dependenciaConsulta = tareas.get(0).getDependencia();
            if (dependenciaConsulta != null && !dependenciaConsulta.isBlank()) {
                String[] dependenciasHijas = dependenciaConsulta.split(",");
                for (String hija : dependenciasHijas) {
                    String nombreHija = hija.trim();
                    agregarDependenciaSiValida(nombreHija, limite);
                }
            }
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
