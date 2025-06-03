package CrearPane;

import ConsultasSQL.ConsultaInstrucciones;
import ConsultasSQL.ConsultasOperaciones;
import ConsultasSQL.ConsultasTareas;
import Controladores.PantallaPrincipalController;
import Objetos.Operacion;
import Objetos.Tareas;
import gestorDeOperaciones.GestorDeOperaciones;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class PaneDinamico {

    private Map<String, CheckBox> checkBoxesMap;
    private Map<String, List<String>> dependenciasPorTarea;
    private PantallaPrincipalController controlador;

    Connection con = GestorDeOperaciones.getConnection();

    public PaneDinamico(Map<String, CheckBox> checkBoxesMap, Map<String, List<String>> dependenciasPorTarea, PantallaPrincipalController controlador) {
        this.checkBoxesMap = checkBoxesMap;
        this.dependenciasPorTarea = dependenciasPorTarea;
        this.controlador = controlador;
    }

    public HBox cargarGestor(String operacion, boolean pausa) {
        ConsultasOperaciones consultasql = new ConsultasOperaciones(con);
        ConsultasTareas consultaSQL = new ConsultasTareas(con);
        ConsultaInstrucciones consultaInstruccion = new ConsultaInstrucciones(con);

        TitledPane titledPane = new TitledPane();
        titledPane.setText(operacion);
        HBox hbox = new HBox();
        VBox vbox = new VBox();

        List<String> dependenciaConsulta = consultasql.buscarTareasPorOperacion(operacion);

        for (String parte : dependenciaConsulta) {
            try {
                List<String> tareaConsultada = consultaInstruccion.instruccionesAsociadas(parte);

                TitledPane titledPaneTarea = new TitledPane();
                titledPaneTarea.setText(parte.trim());

                VBox vboxTareas = new VBox(5);

                if (tareaConsultada != null && !tareaConsultada.isEmpty()) {
                    for (String instruccion : tareaConsultada) {
                        String idCheckBox = parte + "_" + instruccion.trim();

                        CheckBox checkbox = new CheckBox(instruccion.trim());
                        checkbox.setId(idCheckBox);
                        checkbox.setOnAction(event -> controlador.CheckBoxSelect(event));
                        checkbox.setSelected(consultaInstruccion.estaInstruccionCompletada(parte, instruccion));

                        // ✅ Activar/desactivar según pausa
                        checkbox.setDisable(pausa);

                        checkBoxesMap.put(idCheckBox, checkbox);
                        vboxTareas.getChildren().add(checkbox);
                    }

                    // Crear y agregar botones según estado de la tarea
                    Tareas valorTarea = consultaSQL.ConsultaTareas(parte);
                    ButtonBar estados = new ButtonBar();

                    if (valorTarea.getValorPausa()) {
                        Button btnPausa = new Button("⏸");
                        btnPausa.setId(parte + "_btnPausa");
                        btnPausa.setOnAction(event -> controlador.Pausa(event));
                        btnPausa.setDisable(pausa); // ✅ Control por pausa
                        estados.getButtons().add(btnPausa);

                        if (valorTarea.getValorReanudar()) {
                            Button btnReanudar = new Button("▶");
                            btnReanudar.setId(parte + "_btnReanudar");
                            btnReanudar.setOnAction(event -> controlador.Reanudar(event));
                            btnReanudar.setDisable(pausa); // ✅ Control por pausa
                            estados.getButtons().add(btnReanudar);
                        }

                        if (valorTarea.getValorReiniciar()) {
                            Button btnReiniciar = new Button("🔄");
                            btnReiniciar.setId(parte + "_btnReiniciar");
                            btnReiniciar.setOnAction(event -> controlador.Reiniciar(event));
                            btnReiniciar.setDisable(pausa); // ✅ Control por pausa
                            estados.getButtons().add(btnReiniciar);
                        }
                    }

                    vboxTareas.getChildren().add(estados);
                }

                titledPaneTarea.setContent(vboxTareas);
                vbox.getChildren().add(titledPaneTarea);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TextArea resumen = cargarTextoResumen(operacion);
        resumen.setPrefHeight(Region.USE_COMPUTED_SIZE);
        resumen.setMaxHeight(Double.MAX_VALUE);
        resumen.setEditable(false);

        titledPane.setContent(vbox);
        hbox.getChildren().add(titledPane);
        hbox.getChildren().add(resumen);
        return hbox;
    }

    public TextArea cargarTextoResumen(String operacion) {
        ConsultasOperaciones consultasql = new ConsultasOperaciones(con);
        ConsultasTareas consultaSQL = new ConsultasTareas(con);
        ConsultaInstrucciones consultaInstruccion = new ConsultaInstrucciones(con);

        TextArea lbSalida = new TextArea("");
        lbSalida.setEditable(false);
        lbSalida.setWrapText(true);

        // Información de la operación
        List<Operacion> detalleOperacionConsultada = consultasql.ConsultaOperacion(operacion);
        for (Operacion detalleOperacion : detalleOperacionConsultada) {
            int porcentajeOperacion = consultaSQL.obtenerPorcentajeTareasFinalizadas(operacion);
            lbSalida.setText(
                    "OPERACION: " + detalleOperacion.getNombreOperacion()
                    + "\n\nEstado de la operación: " + detalleOperacion.getEstado()
                    + "\nLímite de tareas: " + detalleOperacion.getNumeroTareas() + "\n"
                    + "Porcentaje de avance de la operacion  " + porcentajeOperacion
                    + "% \n\t"
            );
        }

        // Tareas asociadas
        List<String> tareasAsociadas = consultasql.buscarTareasPorOperacion(operacion);

        for (String tarea : tareasAsociadas) {
            Tareas detalleTareaConsultada = consultaSQL.ConsultaTareas(tarea);
            if (detalleTareaConsultada != null) {
                lbSalida.setText(
                        lbSalida.getText()
                        + "\n\tTarea: " + tarea
                        + "\n\t Descripción: " + detalleTareaConsultada.getDescripcion()
                        + "\n\t Estado: " + detalleTareaConsultada.getEstado()
                        + "\n\t\t Instrucciones: ");
            }

            try {
                List<String> instrucciones = consultaInstruccion.instruccionesAsociadas(tarea);
                if (instrucciones != null && !instrucciones.isEmpty()) {
                    for (String instruccion : instrucciones) {
                        boolean completado = consultaInstruccion.estaInstruccionCompletada(tarea, instruccion);
                        lbSalida.setText(lbSalida.getText()
                                + "\n- " + instruccion + ": " + (completado ? "✅ Completada" : "❌ Pendiente"));
                       
                    }
                }

                int porcentaje = consultaInstruccion.obtenerPorcentajeCompletado(tarea);
                 List<Tareas> tareaCompleta= consultaSQL.ConsultaTareasPorOperacion(operacion);
                             for(Tareas tareaDetalle: tareaCompleta){
                                 if(porcentaje ==100){
                                     lbSalida.setText(lbSalida.getText()+
                                             "\nLa tarea fue completada con exito");
                                 }{
                                 lbSalida.setText(lbSalida.getText()+
                                             "\nLa tarea aun no fue completada");
                             }
                                }
                lbSalida.setText(lbSalida.getText()
                        + "\nAvance de la tarea \"" + tarea + "\": " + porcentaje + "%\n");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lbSalida;
    }

}
