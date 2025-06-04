package Controladores;

import ConsultasSQL.ConsultaInstrucciones;
import ConsultasSQL.ConsultasMaximos;
import ConsultasSQL.ConsultasOperaciones;
import Objetos.FilaOperacion;
import Objetos.Operacion;
import Validaciones.AgregarTareas;
import ConsultasSQL.ConsultasTareas;
import CrearPane.PaneDinamico;
import Objetos.FilaTareas;
import Objetos.Maximos;
import Objetos.Tareas;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import gestorDeOperaciones.CrearPane;
import gestorDeOperaciones.CrearPane;
import gestorDeOperaciones.GestorDeOperaciones;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class PantallaPrincipalController {

    Connection con = GestorDeOperaciones.getConnection();
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
    private TableColumn<FilaTareas, String> tbc_EstadoTareas;
    @FXML
    private TableColumn<FilaOperacion, String> tbc_EstadoOperacion;
    @FXML
    private TableView<FilaOperacion> tbv_Operaciones;
    @FXML
    private TableColumn<FilaOperacion, String> tbc_Operaciones;
    @FXML
    private MenuButton menuEjecucion;
    @FXML
    private MenuButton menuCreacion;

    private ObservableList<FilaTareas> filasTarea = FXCollections.observableArrayList();
    private ObservableList<FilaTareas> filasTareaEstado = FXCollections.observableArrayList();
    private ArrayList<String> ListaTareas;
    private ObservableList<FilaOperacion> filasOperaciones = FXCollections.observableArrayList();
    private ObservableList<FilaOperacion> filasOperacionesEstado = FXCollections.observableArrayList();
    @FXML
    private Button btnCerrar;
    private Map<String, CheckBox> checkBoxesMap = new HashMap<>();
    private Map<String, List<String>> dependenciasPorTarea = new HashMap<>();
    private Set<String> tareasPausadas = new HashSet<>();
    private String ejecutar;
    private static PantallaPrincipalController instancia;
    private ConsultaInstrucciones ins;

    @FXML
    public void initialize() {
        instancia = this;
        ConsultasMaximos maximos = new ConsultasMaximos(con);
        maximos.crearMaximos();
        Maximos maximo = maximos.obtenerMaximos();
        if (maximo != null) {
            menuEjecucion.setText(String.valueOf(maximo.getMaximoEjecucion()));
            menuCreacion.setText(String.valueOf(maximo.getMaximoCreacion()));

        }

        tbc_Operaciones.setCellValueFactory(new PropertyValueFactory<>("operacion"));
        tbc_EstadoOperacion.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tbc_Tareas.setCellValueFactory(new PropertyValueFactory<>("tarea"));
        tbc_EstadoTareas.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tbv_Operaciones.setItems(filasOperaciones);
        tbv_Tareas.setItems(filasTarea);
        tbv_Tareas.setSelectionModel(null);
        cargarOperacionesEnTabla();

        tbv_Operaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            ejecutar = null;
            if (newSelection != null) {
                cargarTareasEnTabla(newSelection.getOperacion());
                ejecutar = newSelection.getOperacion();
                btn_Ejecutar.setVisible(true);
                btn_Detener.setVisible(true);
            }
        });

        ConsultasTareas consultas = new ConsultasTareas(con);
        ArrayList<Integer> cantidades = consultas.ListaTareasCantidad();
        if (cantidades.size() >= 3) {
            tf_OperacionesActivas.setText(cantidades.get(1).toString());
            tf_TotalOperaciones.setText(cantidades.get(0).toString());
            tf_TareasEjecucion.setText(cantidades.get(2).toString());
        } else {
            tf_OperacionesActivas.setText("0");
            tf_TotalOperaciones.setText("0");
            tf_TareasEjecucion.setText("0");
        }
    }

    @FXML
    private void Salir(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    public void MenuCreacion(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        String id = item.getText();
        ConsultasMaximos maximo = new ConsultasMaximos(con);
        maximo.actualizarMaximoCreacion(Integer.parseInt(id));
        menuCreacion.setText(id);

    }

    @FXML
    public void MenuEjecucion(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        String id = item.getText();
        ConsultasMaximos maximo = new ConsultasMaximos(con);
        ConsultasOperaciones operacionesTotal = new ConsultasOperaciones(con);
        int totalOperaciones = operacionesTotal.ConsultaOperacionEnEjecucion();
        int totalEjecucion = Integer.parseInt(id);
        if (totalOperaciones < totalEjecucion) {
            maximo.actualizarMaximoEjecucion(totalEjecucion);
            menuEjecucion.setText(id);
        } else {
            Alert alerta = new Alert(Alert.AlertType.ERROR, "No se ha seleccionado ninguna operación para ejecutar.");
            alerta.setTitle("ERROR");
            alerta.setHeaderText("El número de operaciones activas actualmente es mayor");
            alerta.showAndWait();
        }

    }

    public void Menu(ActionEvent event) throws IOException {

        MenuItem item = (MenuItem) event.getSource();
        String id = item.getId();
        CrearPane crear = new CrearPane();
        String fxmlPath = crear.Abrir(id);

        if (!fxmlPath.endsWith(".fxml")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Identificador de pantalla no reconocido");
            alert.setContentText(fxmlPath);
            System.out.println(fxmlPath);
            alert.showAndWait();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage newStage = new Stage();

        String titulo = id.replaceAll("([a-z])([A-Z])", "$1 $2");

        newStage.setTitle(titulo);
        newStage.setScene(scene);
        newStage.show();
    }

    @FXML
    public void Pausa(ActionEvent event) {
        Button boton = (Button) event.getSource();
        String idBoton = boton.getId();
        String tareaId = idBoton.replace("_btnPausa", "");
        tareasPausadas.add(tareaId);

        ConsultaInstrucciones ins = new ConsultaInstrucciones(con);
        List<String> instrucciones = ins.instruccionesAsociadas(tareaId);
        if (instrucciones != null) {
            for (String instruccion : instrucciones) {
                CheckBox checkbox = checkBoxesMap.get(tareaId + "_" + instruccion);
                if (checkbox != null) {
                    checkbox.setDisable(true);
                }
            }
        }
    }

    @FXML
    public void Reanudar(ActionEvent event) {
        Button boton = (Button) event.getSource();
        String idBoton = boton.getId();
        String tareaId = idBoton.replace("_btnReanudar", "");
        tareasPausadas.add(tareaId);

        ConsultaInstrucciones ins = new ConsultaInstrucciones(con);
        List<String> instrucciones = ins.instruccionesAsociadas(tareaId);

        if (instrucciones != null) {
            for (String parte : instrucciones) {
                CheckBox checkbox = checkBoxesMap.get(tareaId + "_" + parte);
                if (checkbox != null) {
                    checkbox.setDisable(false);
                }
            }
        }
    }

    @FXML
    public void Reiniciar(ActionEvent event) {
        Button boton = (Button) event.getSource();
        String idBoton = boton.getId();
        String tareaId = idBoton.replace("_btnReiniciar", "");

        ConsultaInstrucciones ins = new ConsultaInstrucciones(con);
        List<String> instrucciones = ins.instruccionesAsociadas(tareaId);

        ConsultasTareas consultaTarea = new ConsultasTareas(con);
        List<Tareas> listaTareas = consultaTarea.ConsultaTareasPorOperacion(ejecutar);
        Alert alerta;

        if (instrucciones != null) {
            for (String parte : instrucciones) {
                ins.cambiarEstadoInstruccion(parte, "Pendiente");
                CheckBox checkbox = checkBoxesMap.get(tareaId + "_" + parte);
                if (checkbox != null) {
                    checkbox.setSelected(false);
                    checkbox.setDisable(false);
                }
            }
        }

        tareasPausadas.remove(tareaId);
    }

    @FXML
    private void EjecutarOperacion() {
        if (ejecutar == null || ejecutar.trim().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No se ha seleccionado ninguna operación para ejecutar.");
            alerta.setTitle("Atención");
            alerta.setHeaderText("Operación no seleccionada");
            alerta.showAndWait();
            return;
        }

        ConsultasOperaciones consulta = new ConsultasOperaciones(con);
        int totalOperaciones = consulta.ConsultaOperacionEnEjecucion();
        int maximoEjec = Integer.parseInt(menuEjecucion.getText());
        System.out.println("Total de operaciones: " + totalOperaciones);
        System.out.println("Total de operaciones a ejecutar: " + maximoEjec);
        boolean exito = false;
        if (totalOperaciones < maximoEjec) {
            System.out.println("totalOperaciones<maximoEjec ");
            exito = consulta.actualizarEstadoOperacion(ejecutar, "En ejecucion");
        }

        Alert alerta;
        if (!exito) {
            alerta = new Alert(Alert.AlertType.ERROR, "No se pudo ejecutar la operación '" + ejecutar + "'.");
            alerta.setTitle("Error");
            alerta.setHeaderText("Falló la ejecución");
            alerta.showAndWait();
        }
        refrescarComponentesVisuales();
    }

    @FXML
    private void PausarOperacion() {
        if (ejecutar == null || ejecutar.trim().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No se ha seleccionado ninguna operación para ejecutar.");
            alerta.setTitle("Atención");
            alerta.setHeaderText("Operación no seleccionada");
            alerta.showAndWait();
            return;
        }
        ConsultasOperaciones consulta = new ConsultasOperaciones(con);
        boolean exito = consulta.actualizarEstadoOperacion(ejecutar, "En pausa");
        Alert alerta;
        if (!exito) {
            alerta = new Alert(Alert.AlertType.ERROR, "No se pudo pausar la operación '" + ejecutar + "'.");
            alerta.setTitle("Error");
            alerta.setHeaderText("Falló la ejecución");
            alerta.showAndWait();
        }
        refrescarComponentesVisuales();

    }

    @FXML
    private void DetenerOperacion() {
        if (ejecutar == null || ejecutar.trim().isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING, "No se ha seleccionado ninguna operación para detener.");
            alerta.setTitle("Atención");
            alerta.setHeaderText("Operación no seleccionada");
            alerta.showAndWait();
            return;
        }
        ConsultasOperaciones consulta = new ConsultasOperaciones(con);
        boolean exito = consulta.actualizarEstadoOperacion(ejecutar, "Detenida");
        ConsultasTareas consultaTarea = new ConsultasTareas(con);
        List<Tareas> listaTareas = consultaTarea.ConsultaTareasPorOperacion(ejecutar);
        Alert alerta;
        if (exito) {
            for (Tareas tarea : listaTareas) {
                ConsultaInstrucciones instruccion = new ConsultaInstrucciones(con);
                List<String> instrucciones = instruccion.instruccionesAsociadas(tarea.getNombreTarea());
                for (String instruccionVerificar : instrucciones) {
                    instruccion.cambiarEstadoInstruccion(instruccionVerificar, "Pendiente");
                }
            }

        } else {
            alerta = new Alert(Alert.AlertType.ERROR, "No se pudo detener la operación '" + ejecutar + "'.");
            alerta.setTitle("Error");
            alerta.setHeaderText("Falló la detención");
            alerta.showAndWait();
        }

        refrescarComponentesVisuales();
    }

    private void cargarTareasEnTabla(String operacionSeleccionada) {

        ConsultasOperaciones opera = new ConsultasOperaciones(con);
        List<Operacion> validarPausa = opera.ConsultaOperacion(operacionSeleccionada);
        for (Operacion validacion : validarPausa) {
            if (validacion.getEstado().equals("En pausa") || validacion.getEstado().equals("Detenida")) {
                cargarGestor(operacionSeleccionada, true);
            } else {
                cargarGestor(operacionSeleccionada, false);
            }
        }
        filasTarea.clear();

        ConsultasTareas consultasTareas = new ConsultasTareas(con);
        List<Tareas> tareas = consultasTareas.ConsultaTareasPorOperacion(operacionSeleccionada);
        Map<String, List<Tareas>> tareasPorEstado = new LinkedHashMap<>();
        String[] ordenEstados = {"En ejecucion", "En pausa", "Detenida", "No ejecutada", "Finalizada"};
        for (String est : ordenEstados) {
            tareasPorEstado.put(est, new ArrayList<>());
        }
        for (Tareas tarea : tareas) {
            String estadoTarea = tarea.getEstado();
            if (tareasPorEstado.containsKey(estadoTarea)) {
                tareasPorEstado.get(estadoTarea).add(tarea);
            } else {
                tareasPorEstado.computeIfAbsent(estadoTarea, k -> new ArrayList<>()).add(tarea);
            }
        }
        for (String est : ordenEstados) {
            List<Tareas> tareasEstado = tareasPorEstado.get(est);
            if (tareasEstado != null) {
                for (Tareas tarea : tareasEstado) {
                    filasTarea.add(new FilaTareas(tarea.getNombreTarea(), est));
                }
            }
        }
    }

    private void cargarOperacionesEnTabla() {
        filasOperaciones.clear();
       

        ConsultasOperaciones consultas = new ConsultasOperaciones(con);
        ArrayList<String[]> listaOperacion = consultas.ListaOperaciones();

        if (listaOperacion != null && !listaOperacion.isEmpty()) {
            Map<String, List<String[]>> operacionesPorEstado = new LinkedHashMap<>();
            String[] ordenEstados = {"En ejecucion", "En pausa", "Detenida", "No ejecutada", "Finalizada"};
            for (String estado : ordenEstados) {
                operacionesPorEstado.put(estado, new ArrayList<>());
            }
            for (String[] operacion : listaOperacion) {
                String estado = operacion[1];
                if (operacionesPorEstado.containsKey(estado)) {
                    operacionesPorEstado.get(estado).add(operacion);
                } else {
                    operacionesPorEstado.computeIfAbsent(estado, k -> new ArrayList<>()).add(operacion);
                }
            }
            for (String estado : ordenEstados) {
                List<String[]> lista = operacionesPorEstado.get(estado);
                if (lista != null) {
                    for (String[] operacion : lista) {
                        String nombre = operacion[0];
                        SimpleStringProperty operacionProp = new SimpleStringProperty(nombre);
                        filasOperaciones.add(new FilaOperacion(operacionProp, operacion[1]));

                    }
                }
            }
        }
    }

    private void cargarGestor(String operacion, boolean pausa) {
        ConsultasOperaciones opera = new ConsultasOperaciones(con);

        PaneDinamico builder = new PaneDinamico(checkBoxesMap, dependenciasPorTarea, this);
        HBox pane = builder.cargarGestor(operacion, pausa);
        pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        fp_Gestor.getChildren().clear();
        fp_Gestor.getChildren().add(pane);
        pane.prefWidthProperty().bind(fp_Gestor.widthProperty());
        pane.prefHeightProperty().bind(fp_Gestor.heightProperty());

    }

    public static PantallaPrincipalController getInstancia() {
        return instancia;
    }

    public void refrescarComponentesVisuales() {
        filasTarea.clear();
        filasOperaciones.clear();
        fp_Gestor.getChildren().clear();
        initialize();
    }

    @FXML
    public void CheckBoxSelect(ActionEvent event) {
        CheckBox cb = (CheckBox) event.getSource();
        String texto = cb.getId();
        boolean seleccionado = cb.isSelected();

        if (texto == null || !texto.contains("_")) {
            System.out.println("Id del CheckBox inválido.");
            return;
        }

        String[] partes = texto.split("_");
        if (partes.length < 2) {
            System.out.println("Id del CheckBox no contiene información suficiente.");
            return;
        }

        String tareaId = partes[0];
        String instruccion = partes[1];
        ConsultasTareas consultaTarea = new ConsultasTareas(con);
        String nombreOperacion = consultaTarea.obtenerNombreOperacionPorTarea(tareaId);
        try {
            if (ins == null) {
                ins = new ConsultaInstrucciones(con);
            }
            String nuevoEstado = seleccionado ? "Completado" : "Pendiente";
            boolean exito = ins.cambiarEstadoInstruccionPorTarea(tareaId, instruccion, nuevoEstado);
            consultaTarea.ModificarEstado(tareaId, "En ejecucion");
            if (exito) {
                if (exito) {
                    boolean todasInstruccionesCompletas = ins.estanTodasCompletadas(tareaId);
                    if (todasInstruccionesCompletas) {
                        consultaTarea.ModificarEstado(tareaId, "Finalizada");
                    } else {
                        consultaTarea.ModificarEstado(tareaId, "En ejecucion");
                    }

                    ConsultasOperaciones consulta = new ConsultasOperaciones(con);
                    boolean todasTareasFinalizadas = consultaTarea.estanTodasTareasFinalizadas(nombreOperacion);
                    if (todasTareasFinalizadas) {
                        consulta.actualizarEstadoOperacion(nombreOperacion, "Finalizada");
                    } else {
                        consulta.actualizarEstadoOperacion(nombreOperacion, "En ejecucion");
                    }
                }

            }

        } catch (Exception e) {
            System.out.println("⚠️ Error al procesar el CheckBox: " + e.getMessage());
            e.printStackTrace();
        }
        ConsultasOperaciones opera = new ConsultasOperaciones(con);
        List<Operacion> validarPausa = opera.ConsultaOperacion(nombreOperacion);
        for (Operacion validacion : validarPausa) {
            if (validacion.getEstado().equals("En pausa") || validacion.getEstado().equals("Detenida")) {
                cargarGestor(nombreOperacion, true);
            } else {
                cargarGestor(nombreOperacion, false);
            }
        }
        cargarOperacionesEnTabla();

    }

}
