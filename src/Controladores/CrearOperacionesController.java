package Controladores;

import ConsultasSQL.ConsultasMaximos;
import ConsultasSQL.ConsultasOperaciones;
import Objetos.Operacion;
import Validaciones.AgregarTareas;
import ConsultasSQL.ConsultasTareas;
import Objetos.FilaDependencia;
import Objetos.Maximos;
import Validaciones.AgregarOperaciones;
import Objetos.Tareas;
import gestorDeOperaciones.GestorDeOperaciones;
import java.sql.Connection;
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
    private TextField tf_SalidaEsperada;
    @FXML
    private TextField tf_LimiteDeTareas;
    @FXML
    private TableView<FilaDependencia> tbv_Dependencias;
    @FXML
    private TableColumn<FilaDependencia, String> tbc_Dependencias;

    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();
    Connection con = GestorDeOperaciones.getConnection();

    @FXML
    public void initialize() {
        tbc_Dependencias.setCellValueFactory(new PropertyValueFactory<>("dependencia"));
        tbv_Dependencias.setItems(filasDependencia);
        AgregarTareas agregarTareasMenu = new AgregarTareas();
        agregarTareasMenu.cargarTareasEnMenu(con, spm_Tareas, this::SeleccionarMenuItem);

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
            spm_Tareas.getItems().removeIf(item -> item.getText().equals(nombreDependencia));
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
        String salidaEsperada = tf_SalidaEsperada.getText();
        String cadenaDependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        AgregarOperaciones validar = new AgregarOperaciones();
        boolean validacion = validar.validarDatos(nombreOperacion, numeroOperaciones, cadenaDependencias, salidaEsperada);

        if (!validacion) {
            return;
        }

        Operacion operacion = new Operacion(nombreOperacion, numeroOperaciones, salidaEsperada);
        ConsultasOperaciones crearTarea = new ConsultasOperaciones(con);
        ConsultasMaximos maximo = new ConsultasMaximos(con);
        List<String[]> totalOperaciones = crearTarea.ListaOperaciones();
        int cantidadElementos = totalOperaciones.size();
        Maximos operacionConteo = maximo.obtenerMaximos();
        int maximoOperaciones = operacionConteo.getMaximoCreacion();
        boolean creado = false;
        if (cantidadElementos < maximoOperaciones) {
            creado = crearTarea.Guardar(operacion);
        }

        Alert alerta;
        if (creado) {

            for (FilaDependencia fila : filasDependencia) {
                String dependencia = fila.getDependencia();
                ConsultasTareas modificarFK = new ConsultasTareas( con);
                modificarFK.ModificarFK(dependencia, nombreOperacion.trim());
            }
            alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Operación exitosa");
            alerta.setHeaderText(null);
            alerta.setContentText("Los datos se han guardado correctamente.");

            refrescarPantalla();
        } else {
            alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error");
            alerta.setHeaderText(null);
            alerta.setContentText("No se pudieron guardar los datos.");
        }
        alerta.showAndWait();
        PantallaPrincipalController.getInstancia().refrescarComponentesVisuales();

    }

    @FXML
    void Salir(ActionEvent event) {
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }

    private void agregarDependenciaSiValida(String nombreDependencia, int limite) {
        if (filasDependencia.stream().anyMatch(f -> f.getDependencia().equals(nombreDependencia))) {
            return;
        }

        if (filasDependencia.size() >= limite) {
            mostrarAlerta(Alert.AlertType.ERROR, "Límite alcanzado",
                    "Ha alcanzado el límite de tareas para esta operación.");
            return;
        }

        ConsultasTareas consulta = new ConsultasTareas((java.sql.Connection) con);
        Tareas tarea = consulta.ConsultaTareas(nombreDependencia);

        if (tarea != null) {
            filasDependencia.add(new FilaDependencia(tarea.getNombreTarea()));

            String dependenciaConsulta = tarea.getDependencia();
            if (dependenciaConsulta != null && !dependenciaConsulta.isBlank()) {
                String[] dependenciasHijas = dependenciaConsulta.split(",");
                for (String nombreHija : dependenciasHijas) {
                    agregarDependenciaSiValida(nombreHija.trim(), limite);
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

    private void refrescarPantalla() {
        tf_NombreEntrada.clear();
        tf_SalidaEsperada.clear();
        tf_LimiteDeTareas.clear();
        spm_Tareas.setText("Tareas");

        filasDependencia.clear();

        spm_Tareas.getItems().clear();
        AgregarTareas agregarTareasMenu = new AgregarTareas();
        agregarTareasMenu.cargarTareasEnMenu(con, spm_Tareas, this::SeleccionarMenuItem);

        if (!spm_Tareas.getItems().isEmpty()) {
            String textoMenuItem = spm_Tareas.getItems().get(0).getText();
            boolean sinTareas = textoMenuItem.equals("No hay tareas creadas");
            btn_AgregarDependencia.setDisable(sinTareas);
            btn_DescartarDependencia.setDisable(sinTareas);
        } else {
            btn_AgregarDependencia.setDisable(true);
            btn_DescartarDependencia.setDisable(true);
        }
    }

}
