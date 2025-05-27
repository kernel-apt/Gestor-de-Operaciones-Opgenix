package Operaciones;

import Tareas.AgregarFXML;
import Tareas.FilaDependencia;
import Tareas.FilaInstruccion;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CrearOperacionesController {

    @FXML
    private Button btn_AgregarDependencia;

    @FXML
    private Button btn_DescartarDependencia;

    @FXML
    private Button btn_Guardar;

    @FXML
    private Button btn_Salir;

    @FXML
    private SplitMenuButton spm_Tareas;

    @FXML
    private TextField tf_NombreEntrada;

    @FXML
    private TextField tf_LimiteDeTareas;

    @FXML
    private TableView<FilaDependencia> tbv_Dependencias;

    @FXML
    private TableColumn<FilaDependencia, String> tbc_Dependencias;

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
        MenuItem selectedMenuItem = (MenuItem) event.getSource();
        String texto = selectedMenuItem.getText();
        spm_Tareas.setText(texto);
    }

    @FXML
    void AgregarDependencia(ActionEvent event) {
        try {
            int numeroOperaciones = Integer.parseInt(tf_LimiteDeTareas.getText());
            String nombreDependencia = spm_Tareas.getText();

            if (!nombreDependencia.isEmpty() && !nombreDependencia.equals("Tareas")) {
                if (filasDependencia.size() >= numeroOperaciones) {
                    Alert alerta = new Alert(Alert.AlertType.ERROR);
                    alerta.setTitle("Error al asociar tarea");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Ha alcanzado el límite de tareas para esta operación.");
                    alerta.showAndWait();
                    return;
                }

                // Validar que no se repita la dependencia
                if (filasDependencia.stream().anyMatch(f -> f.getDependencia().equals(nombreDependencia))) {
                    Alert alerta = new Alert(Alert.AlertType.WARNING);
                    alerta.setTitle("Dependencia repetida");
                    alerta.setHeaderText(null);
                    alerta.setContentText("Esta tarea ya fue agregada como dependencia.");
                    alerta.showAndWait();
                    return;
                }

                FilaDependencia nueva = new FilaDependencia(nombreDependencia);
                filasDependencia.add(nueva);
                spm_Tareas.setText("Tareas");
            }

            btn_AgregarDependencia.setDisable(true);
            btn_DescartarDependencia.setDisable(true);

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
            System.out.println("No hay ningún elemento seleccionado para eliminar.");
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        Operacion operacion = null;
        int numeroOperaciones = 0;

        // Validar límite de tareas
        try {
            numeroOperaciones = Integer.parseInt(tf_LimiteDeTareas.getText());
        } catch (NumberFormatException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error de entrada");
            alerta.setHeaderText(null);
            alerta.setContentText("No se ha indicado un límite de tareas correcto.");
            alerta.showAndWait();
            return;
        }

        String nombreOperacion = tf_NombreEntrada.getText();
        String cadenaDependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        Agregarfxml validar = new Agregarfxml();
        boolean validacion = validar.validarDatos(nombreOperacion, numeroOperaciones, cadenaDependencias);

        if (!validacion) {
            return;
        }

        operacion = new Operacion(nombreOperacion, numeroOperaciones, cadenaDependencias);
        Consultassql crearTarea = new Consultassql();
        Boolean creado = crearTarea.Guardar(operacion);

        Alert alerta;
        if (creado) {
            alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Operación exitosa");
            alerta.setHeaderText(null);
            alerta.setContentText("Los datos se han guardado correctamente.");
        } else {
            alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pudieron guardar los datos.");
        }
        alerta.showAndWait();
    }

    @FXML
    void Salir(ActionEvent event) {
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }
}
