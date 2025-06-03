/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Validaciones;

import ConsultasSQL.ConsultasTareas;
import java.sql.Connection;
import gestorDeOperaciones.GestorDeOperaciones;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;

import java.util.List;
import javafx.scene.control.Alert;

public class AgregarTareas {

    public static void cargarTareasEnMenu(Connection con, SplitMenuButton spm_Tareas, EventHandler<ActionEvent> handler) {
        ConsultasTareas listaDeTareas = new ConsultasTareas((java.sql.Connection) con);
        List<String> ListaTareas = listaDeTareas.ListaTareasSinOperacion();

        spm_Tareas.getItems().clear();

        if (ListaTareas != null && !ListaTareas.isEmpty()) {
            for (int i = 0; i < ListaTareas.size(); i++) {
                String nombreTarea = ListaTareas.get(i);
                MenuItem menuItem = new MenuItem(nombreTarea);

                if (i % 2 == 0) {
                    menuItem.setStyle("-fx-background-color: #D7EDCE; -fx-text-fill: black;");
                } else {
                    menuItem.setStyle("-fx-background-color: #EDE5CE; -fx-text-fill: black;");
                }

                menuItem.setOnAction(handler);
                spm_Tareas.getItems().add(menuItem);
                spm_Tareas.getItems().add(new SeparatorMenuItem());
            }
        } else {
            MenuItem menuItem = new MenuItem("No hay tareas disponibles");
            spm_Tareas.getItems().add(menuItem);
        }
    }

    public static void cargarTareasEnMenuEditar(Connection con, SplitMenuButton spm_Tareas, EventHandler<ActionEvent> handler, String tarea) {
        ConsultasTareas listaDeTareas = new ConsultasTareas((java.sql.Connection) con);
        List<String> ListaTareas = listaDeTareas.ListaTareasSinOperacion();

        spm_Tareas.getItems().clear();

        if (ListaTareas != null && !ListaTareas.isEmpty()) {
            for (int i = 0; i < ListaTareas.size(); i++) {
                String nombreTarea = ListaTareas.get(i);
                if (!nombreTarea.equals(tarea)) {
                    MenuItem menuItem = new MenuItem(nombreTarea);
                    if (i % 2 == 0) {
                        menuItem.setStyle("-fx-background-color: #D7EDCE; -fx-text-fill: black;");
                    } else {
                        menuItem.setStyle("-fx-background-color: #EDE5CE; -fx-text-fill: black;");
                    }

                    menuItem.setOnAction(handler);
                    spm_Tareas.getItems().add(menuItem);
                    spm_Tareas.getItems().add(new SeparatorMenuItem());
                } else {
                    MenuItem menuItem = new MenuItem("No hay tareas disponibles");
                    spm_Tareas.getItems().add(menuItem);
                }

            }
        } else {
            MenuItem menuItem = new MenuItem("No hay tareas disponibles");
            spm_Tareas.getItems().add(menuItem);
        }
    }

    public boolean validarCampos(String nombreTarea, String descripcion,
            String cadenaInstrucciones, String salida) {
        if (nombreTarea == null || nombreTarea.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "El nombre de la tarea es obligatorio.");
            return false;
        }

        if (descripcion == null || descripcion.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "La descripción es obligatoria.");
            return false;
        }

        if (cadenaInstrucciones == null || cadenaInstrucciones.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "Debe haber al menos una instrucción.");
            return false;
        }
        if (salida == null || salida.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "El nombre de la tarea es obligatorio.");
            return false;
        }

        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
