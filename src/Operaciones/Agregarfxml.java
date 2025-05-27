/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Operaciones;

import Tareas.ConsultasSQL;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;

/**
 *
 * @author parca
 */
public class Agregarfxml {

    public static void cargarOperacionesEnMenu(SplitMenuButton spm_Operacion, EventHandler<ActionEvent> handler) {
        ConsultasSQL listaDeOperacion = new ConsultasSQL();
        List<String> ListaOperacion = listaDeOperacion.ListaTareas();

        spm_Operacion.getItems().clear(); // Limpiar ítems anteriores si es necesario

        if (ListaOperacion != null && !ListaOperacion.isEmpty()) {
            for (int i = 0; i < ListaOperacion.size(); i++) {
                String nombreTarea = ListaOperacion.get(i);
                MenuItem menuItem = new MenuItem(nombreTarea);

                if (i % 2 == 0) {
                    menuItem.setStyle("-fx-background-color: #D7EDCE; -fx-text-fill: black;");
                } else {
                    menuItem.setStyle("-fx-background-color: #EDE5CE; -fx-text-fill: black;");
                }

                menuItem.setOnAction(handler);
                spm_Operacion.getItems().add(menuItem);
                spm_Operacion.getItems().add(new SeparatorMenuItem());
            }
        } else {
            MenuItem menuItem = new MenuItem("No hay tareas creadas");
            spm_Operacion.getItems().add(menuItem);
        }
    }

    public boolean validarDatos(String nombreOperacion, int numeroOperaciones, String cadenaDependencias) {
        if (nombreOperacion == null || nombreOperacion.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "El nombre de la operación es obligatorio.");
            return false;
        }
        if (numeroOperaciones <= 0) {
            mostrarAlerta("Error de validación", "El límite de tareas debe ser mayor que cero.");
            return false;
        }
        if (cadenaDependencias == null || cadenaDependencias.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "Debe haber al menos una tarea.");
            return false;
        }
        return true;
    }

    public void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
