package Operaciones;

import Tareas.AgregarFXML;
import Tareas.ConsultasSQL;
import Tareas.FilaDependencia;
import Tareas.Tareas;
import java.util.List;
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
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        int numeroOperaciones = 0;
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

        Operacion operacion = new Operacion(nombreOperacion, numeroOperaciones, cadenaDependencias);
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
