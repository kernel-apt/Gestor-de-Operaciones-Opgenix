/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Operaciones;

import Tareas.Tareas;
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
public class Consultassql {

    Connection con = GestorDeOperaciones.getConnection();
    private List<String> cadenaOperacion = new ArrayList<>();
    private int idOperacion;
    private String nombreOperacion;
    private int limiteTareas;
    private String tareas;
    private String estado;
    Alert alerta;
    private String nombreTarea;

    public ArrayList<String> ListaTareas() {
        cadenaOperacion.clear();
        try {
            if (con != null) {  // Verificar si se ha establecido una conexión.
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT Nombre FROM tarea");
                while (rs.next()) {
                    nombreTarea = rs.getString("Nombre");
                    cadenaOperacion.add(nombreTarea);
                }
            }
        } catch (SQLException e) {
            alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
            alerta.showAndWait();
        }
        return (ArrayList) cadenaOperacion;
    }
    
    public ArrayList<String> ListaOperaciones() {
        cadenaOperacion.clear();
        try {
            if (con != null) {  // Verificar si se ha establecido una conexión.
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT Nombre FROM operacion");
                while (rs.next()) {
                    nombreTarea = rs.getString("Nombre");
                    cadenaOperacion.add(nombreTarea);
                }
            }
        } catch (SQLException e) {
            alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
            alerta.showAndWait();
        }
        return (ArrayList) cadenaOperacion;
    }
    
    public boolean Guardar(Operacion operacion) {
        Boolean creado = false;
        if (con != null) {
            String sql = "INSERT INTO operacion (Nombre, Limite, Tareas, Estado) VALUES (?, ?, ?,?)";

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, operacion.getNombreOperacion());
                ps.setInt(2, operacion.getNumeroTareas());
                ps.setString(3, operacion.getTareasAsociadas());
                ps.setString(4, "Creado");

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

    public boolean Modificar(Operacion operacion, int id) {
        Boolean modificado = false;
        if (con != null) {
            String sql = "UPDATE operacion SET Nombre = ?, Limite = ?, Tareas = ?, Estado = ? WHERE idOperacion = ?";

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, operacion.getNombreOperacion());
                ps.setInt(2, operacion.getNumeroTareas());
                ps.setString(3, operacion.getTareasAsociadas());
                ps.setString(4, "Creado");
                ps.setInt(5, id);

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
    
    public boolean Eliminar(int id) {
        Boolean modificado = false;
        if (con != null) {
            String sql = "DELETE FROM operacion WHERE idOperacion = ?";

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

    public Operacion ConsultaOperacion() {
        Operacion operacion = null;
        try {
            if (con != null) {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM operacion");
                while (rs.next()) {
                    idOperacion = rs.getInt("idTarea");
                    nombreOperacion = rs.getString("Nombre");
                    limiteTareas = rs.getInt("Limite");
                    tareas = rs.getString("Tareas");
                    operacion = new Operacion(idOperacion, nombreOperacion, limiteTareas, tareas);
                }
            }
        } catch (SQLException e) {
            alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
            alerta.showAndWait();
        }
        return operacion;
    }
}
