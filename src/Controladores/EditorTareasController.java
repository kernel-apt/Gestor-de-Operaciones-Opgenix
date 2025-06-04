package Controladores;

import ConsultasSQL.ConsultaInstrucciones;
import ConsultasSQL.ConsultasTareas;
import Objetos.FilaInstruccion;
import Objetos.FilaDependencia;
import Objetos.FilaTareas;
import Validaciones.AgregarTareas;
import Objetos.Tareas;
import gestorDeOperaciones.GestorDeOperaciones;
import java.sql.Connection;
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

public class EditorTareasController {

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
    private TextField tf_SalidaEsperada;
    @FXML
    private TextField tf_NombreInstruccion;
    @FXML
    private TextField tf_NombreTarea;

    public String nombreInstruccion;
    public String nombreDependencia;
    private ObservableList<FilaInstruccion> filasInstruccion = FXCollections.observableArrayList();
    private ObservableList<FilaDependencia> filasDependencia = FXCollections.observableArrayList();
    private ObservableList<FilaTareas> filasTarea = FXCollections.observableArrayList();
    private ArrayList<String> ListaTareas;
    Connection con = GestorDeOperaciones.getConnection();

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
                String nombreTarea=newSelection.getTarea();
                setControlesHabilitados(true);
                AgregarTareas agregarTareasMenu = new AgregarTareas();
                agregarTareasMenu.cargarTareasEnMenuEditar(con, spm_Tareas, this::SeleccionarMenuItem,nombreTarea);
                MenuItem primerItem = spm_Tareas.getItems().get(0);
                String textoMenuItem = primerItem.getText();

                if (textoMenuItem.equals("No hay tareas disponibles")) {
                    btn_AgregarDependencia.setDisable(true);
                    btn_DescartarDependencia.setDisable(true);
                }
            }
        });

        
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
    nombreInstruccion = tf_NombreInstruccion.getText().trim();

    if (!nombreInstruccion.isEmpty()) {
        boolean yaExiste = filasInstruccion.stream()
                .anyMatch(f -> f.getInstruccion().equalsIgnoreCase(nombreInstruccion));

        if (yaExiste) {
            mostrarAlerta(Alert.AlertType.WARNING, "Instrucción duplicada", "La instrucción ya ha sido agregada.");
            return;
        }

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
        spm_Tareas.getItems().removeIf(item -> item.getText().equals(nombreDependencia));
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
             String nombreTarea = tf_NombreTarea.getText();
             String nombreInstruccion = seleccionado.getInstruccion();
             ConsultaInstrucciones consultaInstruccion = new ConsultaInstrucciones(con);
             boolean existeInstruccion = consultaInstruccion.instruccionExisteParaTarea(nombreInstruccion, nombreTarea);
             boolean eliminacionInstruccion= false;
             if(existeInstruccion){
                 eliminacionInstruccion = consultaInstruccion.eliminarInstruccionEspecifica(nombreTarea, nombreInstruccion);
             }
             if(eliminacionInstruccion){
                 System.out.println("La instruccion fue eliminada de la base de datos");
             } else{
                 System.out.println("La instruccion no fue eliminada de la base de datos");
             }
            filasInstruccion.remove(seleccionado);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No hay ningún elemento seleccionado");
        }
    }

    @FXML
    void Guardar(ActionEvent event) {
        Tareas tarea = null;
        AgregarTareas validar = new AgregarTareas();

        String nombreTarea = tf_NombreTarea.getText();
        String descripcion = tf_Descripcion.getText();
        Boolean valorPausa = cb_Pausa.isSelected();
        Boolean valorReanudar = cb_Reanudar.isSelected();
        String salida = tf_SalidaEsperada.getText();

        String cadenaInstrucciones = filasInstruccion.stream()
                .map(FilaInstruccion::getInstruccion)
                .collect(Collectors.joining(","));

        String cadenaDependencias = filasDependencia.stream()
                .map(FilaDependencia::getDependencia)
                .collect(Collectors.joining(","));

        boolean validacion = validar.validarCampos(nombreTarea, descripcion, cadenaDependencias, salida);

        if (validacion) {
            tarea = new Tareas(nombreTarea, descripcion, valorPausa, valorReanudar, true, cadenaDependencias, salida);
        }
        ConsultaInstrucciones consultaInstruccion = new ConsultaInstrucciones(con);
        ConsultasTareas crearTarea = new ConsultasTareas(con);

        for (FilaInstruccion fila : filasInstruccion) {
            String nombreInstruccion = fila.getInstruccion();
            consultaInstruccion.crearInstruccion(nombreInstruccion, nombreTarea);
            
        }
        
        Boolean creado = crearTarea.Modificar(tarea);
        if (creado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Operación exitosa", "Los datos se han guardado correctamente.");
            refrescarPantalla();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron guardar los datos.");
        }
        PantallaPrincipalController.getInstancia().refrescarComponentesVisuales();

    }

    @FXML
    void Eliminar(ActionEvent event) {
        String nombre = tf_NombreTarea.getText();

        boolean creado = false;
        ConsultasTareas crearTarea = new ConsultasTareas(con);
        ConsultaInstrucciones instrucciones = new ConsultaInstrucciones(con);
        boolean instruccionEliminada = instrucciones.eliminarInstruccionesPorTarea(nombre);
        if (instruccionEliminada) {
            creado = crearTarea.Eliminar(nombre);
        }
        if (creado) {
            refrescarPantalla();
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
        } else {
            cb_Reanudar.setSelected(false);
            cb_Reanudar.setDisable(true);
        }
    }

    private void cargarTareasEnTabla() {
        filasTarea.clear();

        ConsultasTareas consultas = new ConsultasTareas(con);
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
            ConsultasTareas consultaDetalleTarea = new ConsultasTareas(con);
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
                tf_SalidaEsperada.setText(tareaConsultada.getSalida());
                tf_Descripcion.setText(tareaConsultada.getDescripcion());
                filasInstruccion.clear();
                filasDependencia.clear();
                ConsultaInstrucciones instrucciones = new ConsultaInstrucciones(con);
                List<String> instruccionesConsulta = instrucciones.instruccionesAsociadas(comparar);

                for (String instruccionesAsociadas : instruccionesConsulta) {
                    filasInstruccion.add(new FilaInstruccion(instruccionesAsociadas.trim()));
                }

                String dependenciaConsulta = tareaConsultada.getDependencia();
                if (dependenciaConsulta != null && !dependenciaConsulta.trim().isEmpty()) {
                    for (String parte : dependenciaConsulta.split(",")) {
                        filasDependencia.add(new FilaDependencia(parte.trim()));
                    }
                }

                cb_Pausa.setSelected(tareaConsultada.getValorPausa());
                cb_Reanudar.setSelected(tareaConsultada.getValorReanudar());
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
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void refrescarPantalla() {
        tf_NombreTarea.clear();
        tf_Descripcion.clear();
        tf_SalidaEsperada.clear();
        tf_NombreInstruccion.clear();

        cb_Pausa.setSelected(false);
        cb_Reanudar.setSelected(false);

        cb_Reanudar.setDisable(true);

        filasInstruccion.clear();
        filasDependencia.clear();

        spm_Tareas.setText("Tareas");

        cargarTareasEnTabla();
        setControlesHabilitados(false);
    }

}
