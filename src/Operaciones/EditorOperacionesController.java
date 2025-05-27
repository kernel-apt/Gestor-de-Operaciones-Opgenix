package Operaciones;

import Tareas.AgregarFXML;
import Tareas.ConsultasSQL;
import Tareas.FilaDependencia;
import Tareas.FilaInstruccion;
import Tareas.FilaTareas;
import Tareas.Tareas;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class EditorOperacionesController {

    @FXML
    private Button btn_AgregarDependencia;

    @FXML
    private Button btn_DescartarDependencia;

    @FXML
    private Button btn_Eliminar;

    @FXML
    private Button btn_Guardar;

    @FXML
    private Button btn_Salir;

    @FXML
    private SplitMenuButton spm_Tareas;

    @FXML
    private TextArea ta_Precondiciones;

    @FXML
    private TableView<FilaDependencia> tbv_Dependencias;

    @FXML
    private TableColumn<FilaDependencia, String> tbc_Dependencias;

    @FXML
    private TableView<FilaOperacion> tbv_Operaciones;
    @FXML
    private TableColumn<FilaOperacion, String> tbc_Operaciones;

    @FXML
    private TextField tf_LimiteDeTareas;

    @FXML
    private TextField tf_NombreEntrada;

    @FXML
    private TextField tf_Salidas;

    @FXML
    private TextField tf_id;

    public String nombreDependencia;
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
            int numeroOperaciones = Integer.parseInt(tf_LimiteDeTareas.getText());
            String nombreDependencia = spm_Tareas.getText();

            if (!nombreDependencia.isEmpty() && !nombreDependencia.equals("Tareas")) {
                FilaDependencia nueva = new FilaDependencia(nombreDependencia);
                filasDependencia.add(nueva);
                if (filasDependencia.size() <= numeroOperaciones) {
                    spm_Tareas.setText("Tareas");
                } else {
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setTitle("Error al asociar tarea");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Ha alcanzado el limite de tareas que puede agregar a esta operación");
                    alerta.showAndWait();
                }
                btn_AgregarDependencia.setDisable(true);
                btn_DescartarDependencia.setDisable(true);
            }
        } catch (NumberFormatException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de entrada");
            alerta.setHeaderText(null);
            alerta.setContentText("No se ha indicado un límite de tareas correcto.");
            alerta.showAndWait();
        }
    }

    @FXML
    void DescartarDependencia(ActionEvent event) {
        FilaDependencia seleccionado = tbv_Dependencias.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            filasDependencia.remove(seleccionado);
        } else {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de entrada");
            alerta.setHeaderText(null);
            alerta.setContentText("No hay ningún elemento seleccionado para eliminar.");
            alerta.showAndWait();
        }
    }

    @FXML
    void Eliminar(ActionEvent event) {
        int idOperacion = Integer.parseInt(tf_id.getText());
        Consultassql crearTarea = new Consultassql();
        Boolean creado = crearTarea.Eliminar(idOperacion);

        if (creado) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Operación exitosa");
            alerta.setHeaderText(null);
            alerta.setContentText("Los datos se han eliminado correctamente.");
            alerta.showAndWait();
        } else {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pudieron eliminar los datos.");
            alerta.showAndWait();
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        Operacion operacion = null;
        int numeroOperaciones = 0;
        Agregarfxml validar = new Agregarfxml();
        String nombreOperacion = tf_NombreEntrada.getText();
        int idOperacion = Integer.parseInt(tf_id.getText());

        try {
            numeroOperaciones = Integer.parseInt(tf_LimiteDeTareas.getText());
        } catch (NumberFormatException e) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Entrada inválida");
            alerta.setHeaderText(null);
            alerta.setContentText("No se ha indicado un límite de tareas correcto.");
            alerta.showAndWait();
            return; // Cancelar ejecución si el límite es inválido
        }

        String cadenaDependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        boolean validacion = validar.validarDatos(nombreOperacion, numeroOperaciones, cadenaDependencias);
        if (validacion) {
            operacion = new Operacion(nombreOperacion, numeroOperaciones, cadenaDependencias);
        } else {
            return; // Si la validación falla, no continuar
        }

        Consultassql crearTarea = new Consultassql();
        Boolean creado = crearTarea.Modificar(operacion, idOperacion);

        if (creado) {
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Operación exitosa");
            alerta.setHeaderText(null);
            alerta.setContentText("Los datos se han guardado correctamente.");
            alerta.showAndWait();
        } else {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pudieron guardar los datos.");
            alerta.showAndWait();
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
            Operacion operacionConsultada = consultaDetalleOperacion.ConsultaOperacion();

            tf_NombreEntrada.setText(operacionConsultada.getNombreOperacion());
            tf_id.setText(String.valueOf(operacionConsultada.getId()));

            String dependenciaConsulta = operacionConsultada.getTareasAsociadas();
            String[] dependencias = dependenciaConsulta.split(",");
            for (String parte : dependencias) {
                filasDependencia.add(new FilaDependencia(parte));
            }
        }else{
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("No hay operaciones creadas");
            alerta.showAndWait();
        }

    }
}
