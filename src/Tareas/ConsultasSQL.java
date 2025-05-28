/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Tareas;

import gestorDeOperaciones.GestorDeOperaciones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;

/**
 *
 * @author parca
 */
public class ConsultasSQL {

    Connection con = GestorDeOperaciones.getConnection();
    private List<String> cadenaTareas = new ArrayList<>();
    private List<Integer> cantidades = new ArrayList<>();
    Alert alerta;
    String nombreTarea;
    int idTarea;
    String descripcion;
    Boolean pausa;
    Boolean reanudar;
    Boolean reiniciar;
    String dependencia;
    String instruccion;

    public ArrayList<String> ListaTareas() {
        cadenaTareas.clear();
        try {
            if (con != null) {  // Verificar si se ha establecido una conexión.
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT Nombre FROM tarea");
                while (rs.next()) {
                    nombreTarea = rs.getString("Nombre");
                    cadenaTareas.add(nombreTarea);
                }
            }
        } catch (SQLException e) {
            alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
            alerta.showAndWait();
        }
        return (ArrayList) cadenaTareas;
    }

    public ArrayList<Integer> ListaTareasCantidad() {
        if (cantidades == null) {
            cantidades = new ArrayList<>();
        }
        cantidades.clear();
        try {
            if (con != null) {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM operacion");
                while (rs.next()) {
                    int tareasTotales = rs.getInt(1); // Puedes usar índice 1 para COUNT(*)
                    cantidades.add(tareasTotales);
                }

                rs = st.executeQuery("SELECT COUNT(*) FROM operacion WHERE Estado = 'En ejecucion'");
                while (rs.next()) {
                    int tareasActivas = rs.getInt(1);
                    cantidades.add(tareasActivas);
                }

                rs = st.executeQuery("SELECT COUNT(*) FROM tarea WHERE Estado = 'En ejecucion'");
                while (rs.next()) {
                    int tareasActivas = rs.getInt(1);
                    cantidades.add(tareasActivas);
                }
            }
        } catch (SQLException e) {
            alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
            alerta.showAndWait();
        }
        return (ArrayList<Integer>) cantidades;
    }

    public List<Tareas> ConsultaTareas() {
        List<Tareas> listaTareas = new ArrayList<>();
        Tareas tarea;
        try {
            if (con != null) {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM tarea");
                while (rs.next()) {
                    idTarea = rs.getInt("idTarea");
                    nombreTarea = rs.getString("Nombre");
                    descripcion = rs.getString("Descripcion");
                    pausa = rs.getBoolean("Pausa");
                    reanudar = rs.getBoolean("Reanudar");
                    reiniciar = rs.getBoolean("Reiniciar");
                    dependencia = rs.getString("Dependencia");
                    instruccion = rs.getString("Instruccion");
                    tarea = new Tareas(idTarea, nombreTarea, descripcion, pausa, reanudar, reiniciar, dependencia, instruccion);
                    listaTareas.add(tarea);
                }
            }
        } catch (SQLException e) {
            alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
            alerta.showAndWait();
        }
        return listaTareas;
    }

    public List<Tareas> ConsultaTareas(String nombreTareaFiltro) {
        List<Tareas> listaTareas = new ArrayList<>();
        try {
            if (con != null) {
                String sql = "SELECT * FROM tarea WHERE Nombre = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, nombreTareaFiltro);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            idTarea = rs.getInt("idTarea");
                            nombreTarea = rs.getString("Nombre");
                            descripcion = rs.getString("Descripcion");
                            pausa = rs.getBoolean("Pausa");
                            reanudar = rs.getBoolean("Reanudar");
                            reiniciar = rs.getBoolean("Reiniciar");
                            dependencia = rs.getString("Dependencia");
                            instruccion = rs.getString("Instruccion");

                            Tareas tarea = new Tareas(idTarea, nombreTarea, descripcion, pausa, reanudar, reiniciar, dependencia, instruccion);
                            listaTareas.add(tarea);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
            alerta.showAndWait();
        }
        return listaTareas;
    }

    public boolean Guardar(Tareas tarea) {
        Boolean creado = false;
        if (con != null) {
            String sql = "INSERT INTO tareas (Nombre, Descripcion, Pausa, Reanudar, Reiniciar, Dependencia,Instruccion, Estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, tarea.getNombreTarea());
                ps.setString(2, tarea.getDescripcion());
                ps.setBoolean(3, tarea.getValorPausa());
                ps.setBoolean(4, tarea.getValorReanudar());
                ps.setBoolean(5, tarea.getValorReiniciar());
                ps.setString(6, tarea.getDependencia());
                ps.setString(7, tarea.getInstruccion());
                ps.setString(8, "Creado");

                int filasInsertadas = ps.executeUpdate();
                if (filasInsertadas != 0) {
                    creado = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return creado;
    }

    public boolean Modificar(Tareas tarea, int id) {
        Boolean creado = false;
        if (con != null) {
            String sql = "UPDATE operacion SET Nombre = ?, Descripcion = ?, Pausa = ?, Reanudar = ?, Reiniciar = ?, Dependencia = ?,Instruccion = ?, Estado = ? WHERE idTarea = ?";

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, tarea.getNombreTarea());
                ps.setString(2, tarea.getDescripcion());
                ps.setBoolean(3, tarea.getValorPausa());
                ps.setBoolean(4, tarea.getValorReanudar());
                ps.setBoolean(5, tarea.getValorReiniciar());
                ps.setString(6, tarea.getDependencia());
                ps.setString(7, tarea.getInstruccion());
                ps.setString(8, "Modificado");
                ps.setInt(8, id);

                int filasInsertadas = ps.executeUpdate();
                if (filasInsertadas != 0) {
                    creado = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return creado;
    }

    public boolean Eliminar(int id) {
        Boolean modificado = false;
        if (con != null) {
            String sql = "DELETE FROM tarea WHERE idTarea = ?";

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, id);
                int filasInsertadas = ps.executeUpdate();
                if (filasInsertadas != 0) {
                    modificado = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
        return modificado;
    }
}
