package gestorDeOperaciones;

import Operaciones.Consultassql;
import Operaciones.FilaOperacion;
import Operaciones.Operacion;
import Tareas.AgregarFXML;
import Tareas.ConsultasSQL;
import Tareas.FilaTareas;
import Tareas.Tareas;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import gestorDeOperaciones.CrearPane;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PantallaPrincipalController {

    private String text;
    @FXML
    private Button btn_Ejecutar;
    @FXML
    private Button btn_Refrescar;
    @FXML
    private Button btn_Detener;
    @FXML
    private AnchorPane ap_Operaciones;
    @FXML
    private FlowPane fp_Gestor;
    @FXML
    private AnchorPane ap_Principal;
    @FXML
    private AnchorPane ap_TareasDeOperaciones;
    @FXML
    private ScrollPane scp_ProcesoOperaciones;
    @FXML
    private TextField tf_OperacionesActivas;
    @FXML
    private TextField tf_TareasEjecucion;
    @FXML
    private TextField tf_TotalOperaciones;
    @FXML
    private TableView<FilaTareas> tbv_Tareas;
    @FXML
    private TableColumn<FilaTareas, String> tbc_Tareas;
    @FXML
    private TableView<FilaOperacion> tbv_Operaciones;
    @FXML
    private TableColumn<FilaOperacion, String> tbc_Operaciones;
    private ObservableList<FilaTareas> filasTarea = FXCollections.observableArrayList();
    private ArrayList<String> ListaTareas;
    private ObservableList<FilaOperacion> filasOperaciones = FXCollections.observableArrayList();
    @FXML
    private Button btnCerrar;
    private Map<String, CheckBox> checkBoxesMap = new HashMap<>();
    private Map<String, List<String>> dependenciasPorTarea = new HashMap<>();
    private Set<String> tareasPausadas = new HashSet<>();
    private String ejecutar;

    @FXML
    public void initialize() {
        tbc_Operaciones.setCellValueFactory(new PropertyValueFactory<>("operacion"));
        tbc_Tareas.setCellValueFactory(new PropertyValueFactory<>("tarea"));
        tbv_Operaciones.setItems(filasOperaciones);
        tbv_Tareas.setItems(filasTarea);
        cargarOperacionesEnTabla();
        btn_Ejecutar.setVisible(false);
        btn_Detener.setVisible(false);
        tbv_Operaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            ejecutar = null;
            if (newSelection != null) {
                cargarTareasEnTabla(newSelection.getOperacion());
                ejecutar = newSelection.getOperacion();
                btn_Ejecutar.setVisible(true);
                btn_Detener.setVisible(true);
            }
        });
        tbv_Tareas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Acciones según la tarea seleccionada
            }
        });
        ConsultasSQL consultas = new ConsultasSQL();
        ArrayList<Integer> cantidades = consultas.ListaTareasCantidad();
        if (cantidades.size() >= 2) {
            tf_OperacionesActivas.setText(cantidades.get(1).toString());
            tf_TotalOperaciones.setText(cantidades.get(0).toString());
        } else {
            tf_OperacionesActivas.setText("0");
            tf_TotalOperaciones.setText("0");
        }
    }

    @FXML
    private void RefrescarPantalla(ActionEvent event) {
        filasTarea.clear();
        filasOperaciones.clear();
        fp_Gestor.getChildren().clear();
        initialize();
    }

    @FXML
    private void Salir(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void Menu(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        String id = item.getId();
        CrearPane crear = new CrearPane();
        String text = crear.Abrir(id);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(text));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage newStage = new Stage();
            newStage.setTitle("Nueva Ventana");
            newStage.setScene(scene);
            newStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la pantalla principal");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void Pausa(ActionEvent event) {
        Button boton = (Button) event.getSource();
        String idBoton = boton.getId();
        tareasPausadas.add(idBoton);
        aplicarAccionACheckbox(idBoton, cb -> cb.setDisable(true));
    }

    @FXML
    public void Reanudar(ActionEvent event) {
        Button boton = (Button) event.getSource();
        String idBoton = boton.getId();
        tareasPausadas.remove(idBoton);
        actualizarEstadosDeDependencias(checkBoxesMap, dependenciasPorTarea);
    }

    @FXML
    public void Reiniciar(ActionEvent event) {
        Button boton = (Button) event.getSource();
        String idBoton = boton.getId();
        if (tareasPausadas.contains(idBoton)) {
            return;
        }
        aplicarAccionACheckbox(idBoton, cb -> cb.setSelected(false));
        actualizarEstadosDeDependencias(checkBoxesMap, dependenciasPorTarea);
    }

    @FXML
    private void Ejecutar() {
        if (ejecutar == null || ejecutar.trim().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No se ha seleccionado ninguna operación para ejecutar.");
            alerta.setTitle("Atención");
            alerta.setHeaderText("Operación no seleccionada");
            alerta.showAndWait();
            return;
        }
        Consultassql consulta = new Consultassql();
        boolean exito = consulta.activarOperacion(ejecutar);
        Alert alerta;
        if (exito) {
            alerta = new Alert(Alert.AlertType.INFORMATION, "La operación '" + ejecutar + "' se ha puesto en ejecución.");
            alerta.setTitle("Éxito");
            alerta.setHeaderText("Operación ejecutada");
        } else {
            alerta = new Alert(Alert.AlertType.ERROR, "No se pudo ejecutar la operación '" + ejecutar + "'.");
            alerta.setTitle("Error");
            alerta.setHeaderText("Falló la ejecución");
        }
        alerta.showAndWait();
    }

    @FXML
    private void Detener() {
        if (ejecutar == null || ejecutar.trim().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No se ha seleccionado ninguna operación para detener.");
            alerta.setTitle("Atención");
            alerta.setHeaderText("Operación no seleccionada");
            alerta.showAndWait();
            return;
        }
        Consultassql consulta = new Consultassql();
        boolean exito = consulta.detener(ejecutar);
        Alert alerta;
        if (exito) {
            alerta = new Alert(Alert.AlertType.INFORMATION, "La operación '" + ejecutar + "' se ha detenido (estado cambiado a 'Creado').");
            alerta.setTitle("Éxito");
            alerta.setHeaderText("Operación detenida");
        } else {
            alerta = new Alert(Alert.AlertType.ERROR, "No se pudo detener la operación '" + ejecutar + "'.");
            alerta.setTitle("Error");
            alerta.setHeaderText("Falló la detención");
        }
        alerta.showAndWait();
    }

    private void cargarTareasEnTabla(String operacionSeleccionada) {
        filasTarea.clear();
        Consultassql consultasSql = new Consultassql();
        List<Operacion> operaciones = consultasSql.ConsultaOperacion(operacionSeleccionada);
        if (operaciones != null && !operaciones.isEmpty()) {
            Operacion operacion = operaciones.get(0);
            String tareasStr = operacion.getTareasAsociadas();
            if (tareasStr != null && !tareasStr.trim().isEmpty()) {
                String[] tareasArray = tareasStr.split(",");
                for (String tarea : tareasArray) {
                    filasTarea.add(new FilaTareas(tarea.trim()));
                }
            } else {
                filasTarea.add(new FilaTareas("No hay tareas asociadas a esta operación"));
            }
        } else {
            filasTarea.add(new FilaTareas("No se encontró la operación o no está en ejecución"));
        }
    }

    private void cargarOperacionesEnTabla() {
        filasOperaciones.clear();
        fp_Gestor.getChildren().clear();
        Consultassql consultas = new Consultassql();
        ArrayList<String> listaOperacion = consultas.ListaOperaciones();
        if (listaOperacion != null && !listaOperacion.isEmpty()) {
            for (String operacion : listaOperacion) {
                filasOperaciones.add(new FilaOperacion(operacion));
                cargarGestor(operacion);
            }
        } else {
            filasOperaciones.add(new FilaOperacion("No existen operaciones actualmente"));
        }
    }

    private void cargarGestor(String operacion) {
        Consultassql consultasql = new Consultassql();
        ConsultasSQL consultaSQL = new ConsultasSQL();
        List<Operacion> operacionConsultada = consultasql.ConsultaOperacionActiva(operacion);
        checkBoxesMap.clear();
        dependenciasPorTarea.clear();
        if (operacionConsultada != null && !operacionConsultada.isEmpty()) {
            for (Operacion operacionExtraer : operacionConsultada) {
                TitledPane titledPane = new TitledPane();
                titledPane.setText(operacionExtraer.getNombreOperacion());
                VBox vbox = new VBox(5);
                String dependenciaConsulta = operacionExtraer.getTareasAsociadas();
                String[] dependencias = dependenciaConsulta.split(",");
                for (String parte : dependencias) {
                    try {
                        List<Tareas> tareaConsultada = consultaSQL.ConsultaTareas(parte.trim());
                        if (tareaConsultada != null && !tareaConsultada.isEmpty()) {
                            TitledPane titledPaneTarea = new TitledPane();
                            titledPaneTarea.setText(parte.trim());
                            VBox vboxTareas = new VBox(5);
                            for (Tareas tareaExtraer : tareaConsultada) {
                                String[] instrucciones = tareaExtraer.getInstruccion().split(",");
                                String tareaClave = operacionExtraer.getNombreOperacion() + "_" + parte.trim();
                                List<String> depsList = new ArrayList<>();
                                if (tareaExtraer.getDependencia() != null && !tareaExtraer.getDependencia().isBlank()) {
                                    depsList = Arrays.stream(tareaExtraer.getDependencia().split(","))
                                            .map(String::trim)
                                            .collect(Collectors.toList());
                                }
                                dependenciasPorTarea.put(tareaClave, depsList);
                                for (String instruccion : instrucciones) {
                                    String idCheckBox = tareaClave + "_" + instruccion.trim();
                                    CheckBox checkbox = new CheckBox(instruccion.trim());
                                    checkbox.setId(idCheckBox);
                                    checkbox.setDisable(!depsList.isEmpty());
                                    vboxTareas.getChildren().add(checkbox);
                                    checkBoxesMap.put(idCheckBox, checkbox);
                                }
                                ButtonBar estados = new ButtonBar();
                                if (tareaExtraer.getValorPausa()) {
                                    Button btnPausa = new Button("⏸");
                                    btnPausa.setId(tareaClave);
                                    btnPausa.setOnAction(this::Pausa);
                                    estados.getButtons().add(btnPausa);
                                    Button btnReanudar = new Button("▶");
                                    btnReanudar.setId(tareaClave);
                                    btnReanudar.setOnAction(this::Reanudar);
                                    estados.getButtons().add(btnReanudar);
                                    Button btnReiniciar = new Button("🔄");
                                    btnReiniciar.setId(tareaClave);
                                    btnReiniciar.setOnAction(this::Reiniciar);
                                    estados.getButtons().add(btnReiniciar);
                                }
                                vboxTareas.getChildren().add(estados);
                            }
                            titledPaneTarea.setContent(vboxTareas);
                            vbox.getChildren().add(titledPaneTarea);
                        }
                    } catch (Exception e) {
                        Alert alerta = new Alert(Alert.AlertType.ERROR, "Error al consultar tarea: " + parte.trim() + "\n" + e.getMessage());
                        alerta.showAndWait();
                    }
                }
                titledPane.setContent(vbox);
                fp_Gestor.getChildren().add(titledPane);
            }
            actualizarEstadosDeDependencias(checkBoxesMap, dependenciasPorTarea);
        } else {
            filasOperaciones.add(new FilaOperacion("No existen operaciones actualmente"));
        }
    }

    private void aplicarAccionACheckbox(String tareaClave, java.util.function.Consumer<CheckBox> accion) {
        checkBoxesMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(tareaClave))
                .forEach(e -> accion.accept(e.getValue()));
    }

    private void actualizarEstadosDeDependencias(Map<String, CheckBox> checkBoxesMap, Map<String, List<String>> dependenciasPorTarea) {
        for (Map.Entry<String, List<String>> entry : dependenciasPorTarea.entrySet()) {
            String tareaClave = entry.getKey();
            List<String> dependencias = entry.getValue();
            boolean todasDependenciasMarcadas = true;
            for (String dep : dependencias) {
                String depPrefix = tareaClave.split("_")[0] + "_" + dep;
                boolean algunaInstruccionMarcada = checkBoxesMap.entrySet().stream()
                        .filter(e -> e.getKey().startsWith(depPrefix))
                        .anyMatch(e -> e.getValue().isSelected());
                if (!algunaInstruccionMarcada) {
                    todasDependenciasMarcadas = false;
                    break;
                }
            }
            boolean habilitar = todasDependenciasMarcadas;
            checkBoxesMap.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(tareaClave))
                    .forEach(e -> e.getValue().setDisable(!habilitar));
        }
    }
}
