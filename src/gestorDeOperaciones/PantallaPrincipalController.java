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
import java.util.List;
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
    public void initialize() {
        tbc_Operaciones.setCellValueFactory(new PropertyValueFactory<>("operacion"));
        tbv_Operaciones.setItems(filasOperaciones);
        cargarOperacionesEnTabla();

        tbv_Operaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                tbc_Tareas.setCellValueFactory(new PropertyValueFactory<>("tarea")); // "tarea" es el nombre del atributo en FilaTareas
                tbv_Tareas.setItems(filasTarea); // Vinculas la tabla con la lista ObservableList

                cargarTareasEnTabla();
            }
        });
        tbv_Tareas.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                //Operaciones
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
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la pantalla principal");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void cargarTareasEnTabla() {
        filasTarea.clear(); // Limpia los datos anteriores

        ConsultasSQL consultas = new ConsultasSQL();
        ArrayList<String> listaTareas = consultas.ListaTareas();

        if (listaTareas != null && !listaTareas.isEmpty()) {
            for (String tarea : listaTareas) {
                filasTarea.add(new FilaTareas(tarea));
            }
        } else {
            filasTarea.add(new FilaTareas("No existen tareas actualmente"));

        }
    }

    private void cargarOperacionesEnTabla() {
        filasOperaciones.clear();

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

    public void cargarGestor(String operacion) {
        Consultassql consultasql = new Consultassql();
        ConsultasSQL consultaSQL = new ConsultasSQL();
        List<Operacion> operacionConsultada = consultasql.ConsultaOperacion(operacion);

        fp_Gestor.getChildren().clear();

        if (operacionConsultada != null && !operacionConsultada.isEmpty()) {
            for (Operacion operacionExtraer : operacionConsultada) {
                TitledPane titledPane = new TitledPane();
                titledPane.setText(operacionExtraer.getNombreOperacion());

                VBox vbox = new VBox();
                vbox.setSpacing(5);

                String dependenciaConsulta = operacionExtraer.getTareasAsociadas();
                String[] dependencias = dependenciaConsulta.split(",");

                for (String parte : dependencias) {
                    try {
                        List<Tareas> tareaConsultada = consultaSQL.ConsultaTareas(parte.trim());
                        if (tareaConsultada != null && !tareaConsultada.isEmpty()) {

                            TitledPane titledPaneTarea = new TitledPane();
                            titledPaneTarea.setText(parte.trim());

                            VBox vboxTareas = new VBox();
                            vboxTareas.setSpacing(5);

                            for (Tareas tareaExtraer : tareaConsultada) {
                                String tareaConsulta = tareaExtraer.getInstruccion();
                                String[] instrucciones = tareaConsulta.split(",");

                                for (String instruccionesTareas : instrucciones) {
                                    CheckBox checkbox = new CheckBox(instruccionesTareas.trim());
                                    checkbox.setId(titledPaneTarea.getText() + "_" + instruccionesTareas.trim());
                                    vboxTareas.getChildren().add(checkbox);
                                }

                                // Botones con texto claro
                                ButtonBar estados = new ButtonBar();

                                Button btn_Pausa = new Button("Pausar");
                                Button btn_Reanudar = new Button("Reanudar");
                                Button btn_Reiniciar = new Button("Reiniciar");

                                estados.getButtons().addAll(btn_Pausa, btn_Reanudar, btn_Reiniciar);
                                vboxTareas.getChildren().add(estados);
                            }

                            titledPaneTarea.setContent(vboxTareas);
                            vbox.getChildren().add(titledPaneTarea);
                        }

                    } catch (Exception e) {
                        System.err.println("Error al consultar tarea '" + parte.trim() + "': " + e.getMessage());
                        Alert alerta = new Alert(Alert.AlertType.ERROR, "Error al consultar tarea: " + parte.trim() + "\n" + e.getMessage());
                        alerta.showAndWait();
                    }
                }

                titledPane.setContent(vbox);
                fp_Gestor.getChildren().add(titledPane);
            }
        } else {
            System.out.println("No se encontraron operaciones para: " + operacion);
        }
    }

}
