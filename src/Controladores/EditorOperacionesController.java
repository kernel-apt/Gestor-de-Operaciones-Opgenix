package Controladores;

import ConsultasSQL.ConsultaInstrucciones;
import ConsultasSQL.ConsultasOperaciones;
import Objetos.Operacion;
import Objetos.FilaOperacion;
import Validaciones.AgregarTareas;
import ConsultasSQL.ConsultasTareas;
import Objetos.FilaDependencia;
import Objetos.FilaTareas;
import Validaciones.AgregarOperaciones;
import Objetos.Tareas;
import gestorDeOperaciones.GestorDeOperaciones;
import java.sql.Connection;
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
    private TextField tf_SalidaEsperada;

    private Connection con = GestorDeOperaciones.getConnection();
    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();
    private ObservableList<FilaOperacion> filasOperaciones = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        tbc_Dependencias.setCellValueFactory(new PropertyValueFactory<>("dependencia"));
        tbv_Dependencias.setItems(filasDependencia);
        tbc_Operaciones.setCellValueFactory(new PropertyValueFactory<>("operacion"));
        tbv_Operaciones.setItems(filasOperaciones);
        cargarOperacionesEnTabla();
        AgregarTareas agregarTareasMenu = new AgregarTareas();
        agregarTareasMenu.cargarTareasEnMenu(con, spm_Tareas, this::SeleccionarMenuItem);
        tbv_Operaciones.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                cargarDatos(newSelection);
                setControlesHabilitados(true);
            }
        });

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
            ConsultasTareas modificarFK = new ConsultasTareas(con); // ✅ Corregido
            modificarFK.ModificarFK(seleccionado.getDependencia(), null); // ✅ No más NullPointerException
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de entrada", "No hay ningún elemento seleccionado para eliminar.");
        }
    }

    @FXML
    void Eliminar(ActionEvent event) {
        AgregarOperaciones utilidades = new AgregarOperaciones();

        try {
            String nombreOperacion = tf_NombreEntrada.getText();

            for (FilaDependencia nombreTarea : filasDependencia) {
                ConsultasTareas crearTarea = new ConsultasTareas(con);
                ConsultaInstrucciones instrucciones = new ConsultaInstrucciones(con);
                instrucciones.eliminarInstruccionesPorTarea(nombreTarea.getDependencia());

                crearTarea.Eliminar(nombreTarea.getDependencia());

            }

            ConsultasOperaciones crearTarea = new ConsultasOperaciones(con);
            boolean creado = crearTarea.Eliminar(nombreOperacion);

            if (creado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa", "Los datos se han eliminado correctamente.");
                refrescarPantalla();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron eliminar los datos.");
            }
            PantallaPrincipalController.getInstancia().refrescarComponentesVisuales();
        } catch (NumberFormatException e) {
            utilidades.mostrarAlerta("Formato inválido", "El ID debe ser un número.");
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        AgregarOperaciones utilidades = new AgregarOperaciones();
        String nombreOperacion = tf_NombreEntrada.getText().trim();
        String salidaEsperada = tf_SalidaEsperada.getText().trim();

        if (!tf_LimiteDeTareas.getText().matches("\\d+")) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "El límite de tareas debe ser un número.");
            return;
        }

        int numeroOperaciones = Integer.parseInt(tf_LimiteDeTareas.getText());
        String cadenaDependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        if (!utilidades.validarDatos(nombreOperacion, numeroOperaciones, cadenaDependencias, salidaEsperada)) {
            return;
        }

        Operacion operacion = new Operacion(nombreOperacion, numeroOperaciones, salidaEsperada);
        ConsultasOperaciones consultas = new ConsultasOperaciones(con);
        boolean creado = consultas.Modificar(operacion);

        if (creado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa", "Los datos se han guardado correctamente.");
            refrescarPantalla();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron guardar los datos.");
        }
        PantallaPrincipalController.getInstancia().refrescarComponentesVisuales();
    }

    @FXML
    void Salir(ActionEvent event) {
        Stage stage = (Stage) btn_Salir.getScene().getWindow();
        stage.close();
    }

    private void cargarOperacionesEnTabla() {
        filasOperaciones.clear();
        ConsultasOperaciones consultas = new ConsultasOperaciones(con);
        ArrayList<String[]> listaOperacion = consultas.ListaOperaciones();

        if (listaOperacion != null && !listaOperacion.isEmpty()) {
            for (String[] operacion : listaOperacion) {
                String nombre = operacion[0];
                filasOperaciones.add(new FilaOperacion(nombre));
            }

        }
    }

    private void cargarDatos(FilaOperacion operacionSeleccionada) {
        String comparar = operacionSeleccionada.getOperacion();

        if (comparar != null && !comparar.isBlank() && !comparar.equals("No existen operaciones actualmente")) {
            ConsultasOperaciones consultaDetalleOperacion = new ConsultasOperaciones(con);
            List<Operacion> operacionesConsultadas = consultaDetalleOperacion.ConsultaOperacion(comparar);

            Operacion operacionConsultada = operacionesConsultadas.stream()
                    .filter(op -> op.getNombreOperacion().equals(comparar))
                    .findFirst()
                    .orElse(null);

            if (operacionConsultada != null) {
                tf_NombreEntrada.setText(operacionConsultada.getNombreOperacion());
                tf_LimiteDeTareas.setText(String.valueOf(operacionConsultada.getNumeroTareas()));
                filasDependencia.clear();
                tf_SalidaEsperada.setText(operacionConsultada.getSalida());
                List<String> tareasAsociadas = consultaDetalleOperacion.buscarTareasPorOperacion(comparar);
                for (String tarea : tareasAsociadas) {
                    filasDependencia.add(new FilaDependencia(tarea));
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
            return;
        }

        if (filasDependencia.size() >= limite) {
            mostrarAlerta(Alert.AlertType.ERROR, "Límite alcanzado",
                    "Ha alcanzado el límite de tareas para esta operación.");
            return;
        }

        ConsultasTareas consulta = new ConsultasTareas(con);
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

    public void refrescarPantalla() {
        filasOperaciones.clear();
        filasDependencia.clear();
        tf_NombreEntrada.clear();
        tf_LimiteDeTareas.clear();
        tf_SalidaEsperada.clear();
        spm_Tareas.setText("Tareas");

        cargarOperacionesEnTabla();
        setControlesHabilitados(false);

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

    private void setControlesHabilitados(boolean habilitado) {
        btn_AgregarDependencia.setDisable(!habilitado);
        btn_DescartarDependencia.setDisable(!habilitado);
        btn_Eliminar.setDisable(!habilitado);
        btn_Guardar.setDisable(!habilitado);
        spm_Tareas.setDisable(!habilitado);
        ta_Precondiciones.setDisable(!habilitado);
        tf_LimiteDeTareas.setDisable(!habilitado);
        tf_NombreEntrada.setDisable(!habilitado);
        tf_SalidaEsperada.setDisable(!habilitado);
    }
}
