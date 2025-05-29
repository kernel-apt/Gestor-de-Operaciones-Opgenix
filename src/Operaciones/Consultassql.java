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

public class Consultassql {

    Connection con = GestorDeOperaciones.getConnection();
    private List<String> cadenaOperacion = new ArrayList<>();
    Alert alerta;

    public ArrayList<String> ListaTareas() {
        cadenaOperacion.clear();
        if (con != null) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT Nombre FROM tarea")) {
                while (rs.next()) {
                    String nombreTarea = rs.getString("Nombre");
                    cadenaOperacion.add(nombreTarea);
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return (ArrayList<String>) cadenaOperacion;
    }

    public ArrayList<String> ListaOperaciones() {
        cadenaOperacion.clear();
        if (con != null) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT Nombre FROM operacion")) {
                while (rs.next()) {
                    String nombreOperacion = rs.getString("Nombre");
                    cadenaOperacion.add(nombreOperacion);
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return (ArrayList<String>) cadenaOperacion;
    }

    public boolean Guardar(Operacion operacion) {
        boolean creado = false;
        if (con != null) {
            String sql = "INSERT INTO operacion (Nombre, Limite, Tareas, Estado) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, operacion.getNombreOperacion());
                ps.setInt(2, operacion.getNumeroTareas());
                ps.setString(3, operacion.getTareasAsociadas());
                ps.setString(4, "Creado");
                int filasInsertadas = ps.executeUpdate();
                if (filasInsertadas != 0) {
                    creado = true;
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al guardar la operación: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return creado;
    }

    public boolean Modificar(Operacion operacion, int id) {
        boolean modificado = false;
        if (con != null) {
            String sql = "UPDATE operacion SET Nombre = ?, Limite = ?, Tareas = ?, Estado = ? WHERE idOperacion = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, operacion.getNombreOperacion());
                ps.setInt(2, operacion.getNumeroTareas());
                ps.setString(3, operacion.getTareasAsociadas());
                ps.setString(4, "Modificado");
                ps.setInt(5, id);
                int filasActualizadas = ps.executeUpdate();
                if (filasActualizadas != 0) {
                    modificado = true;
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al modificar la operación: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return modificado;
    }

    public boolean activarOperacion(String nombreOperacion) {
        boolean actualizado = false;
        if (con != null) {
            String sqlSelect = "SELECT idOperacion, Tareas FROM operacion WHERE Nombre = ?";
            try (PreparedStatement psSelect = con.prepareStatement(sqlSelect)) {
                psSelect.setString(1, nombreOperacion);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        int idOperacion = rs.getInt("idOperacion");
                        String tareasAsociadas = rs.getString("Tareas");

                        // Actualizar estado de la operación
                        String sqlUpdateOperacion = "UPDATE operacion SET Estado = ? WHERE idOperacion = ?";
                        try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateOperacion)) {
                            psUpdate.setString(1, "En ejecucion");
                            psUpdate.setInt(2, idOperacion);
                            int filasActualizadas = psUpdate.executeUpdate();
                            actualizado = filasActualizadas > 0;
                        }

                        // Actualizar estado de las tareas asociadas
                        if (tareasAsociadas != null && !tareasAsociadas.trim().isEmpty()) {
                            String[] tareas = tareasAsociadas.split(",");
                            String sqlUpdateTarea = "UPDATE tarea SET Estado = ? WHERE Nombre = ?";
                            try (PreparedStatement psUpdateTarea = con.prepareStatement(sqlUpdateTarea)) {
                                for (String tarea : tareas) {
                                    psUpdateTarea.setString(1, "En ejecucion");
                                    psUpdateTarea.setString(2, tarea.trim());
                                    psUpdateTarea.executeUpdate(); // se actualiza una por una
                                }
                            }
                        }
                    } else {
                        System.out.println("No se encontró operación con nombre: " + nombreOperacion);
                    }
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al activar la operación: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return actualizado;
    }

    public boolean detener(String nombreOperacion) {
        boolean actualizado = false;
        if (con != null) {
            String sqlSelect = "SELECT idOperacion, Tareas FROM operacion WHERE Nombre = ?";
            try (PreparedStatement psSelect = con.prepareStatement(sqlSelect)) {
                psSelect.setString(1, nombreOperacion);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        int idOperacion = rs.getInt("idOperacion");
                        String tareasAsociadas = rs.getString("Tareas");

                        // Actualizar estado de la operación
                        String sqlUpdateOperacion = "UPDATE operacion SET Estado = ? WHERE idOperacion = ?";
                        try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateOperacion)) {
                            psUpdate.setString(1, "Creado");
                            psUpdate.setInt(2, idOperacion);
                            int filasActualizadas = psUpdate.executeUpdate();
                            actualizado = filasActualizadas > 0;
                        }

                        // Actualizar estado de las tareas asociadas
                        if (tareasAsociadas != null && !tareasAsociadas.trim().isEmpty()) {
                            String[] tareas = tareasAsociadas.split(",");
                            String sqlUpdateTarea = "UPDATE tarea SET Estado = ? WHERE Nombre = ?";
                            try (PreparedStatement psUpdateTarea = con.prepareStatement(sqlUpdateTarea)) {
                                for (String tarea : tareas) {
                                    psUpdateTarea.setString(1, "Creado");
                                    psUpdateTarea.setString(2, tarea.trim());
                                    psUpdateTarea.executeUpdate();
                                }
                            }
                        }
                    } else {
                        System.out.println("No se encontró operación con nombre: " + nombreOperacion);
                    }
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al detener la operación: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return actualizado;
    }

    public boolean Eliminar(int id) {
        boolean eliminado = false;
        if (con != null) {
            String sql = "DELETE FROM operacion WHERE idOperacion = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                int filasEliminadas = ps.executeUpdate();
                if (filasEliminadas != 0) {
                    eliminado = true;
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al eliminar la operación: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return eliminado;
    }

    public List<Operacion> ConsultaOperacion() {
        List<Operacion> operaciones = new ArrayList<>();
        if (con != null) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM operacion")) {
                while (rs.next()) {
                    int idOperacion = rs.getInt("idOperacion");
                    String nombreOperacion = rs.getString("Nombre");
                    int limiteTareas = rs.getInt("Limite");
                    String tareas = rs.getString("Tareas");
                    Operacion operacion = new Operacion(idOperacion, nombreOperacion, limiteTareas, tareas);
                    operaciones.add(operacion);
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return operaciones;
    }

    public List<Operacion> ConsultaOperacion(String consultaOperacion) {
        List<Operacion> operaciones = new ArrayList<>();
        if (con != null) {
            String sql = "SELECT * FROM operacion WHERE Nombre = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, consultaOperacion);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idOperacion = rs.getInt("idOperacion");
                        String nombreOperacion = rs.getString("Nombre");
                        int limiteTareas = rs.getInt("Limite");
                        String tareas = rs.getString("Tareas");
                        Operacion operacion = new Operacion(idOperacion, nombreOperacion, limiteTareas, tareas);
                        operaciones.add(operacion);
                    }
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return operaciones;
    }
    
    
        public List<Operacion> ConsultaOperacionActiva(String consultaOperacion) {
        List<Operacion> operaciones = new ArrayList<>();
        if (con != null) {
            String sql = "SELECT * FROM operacion WHERE Nombre = ? AND Estado ='En ejecucion'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, consultaOperacion);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idOperacion = rs.getInt("idOperacion");
                        String nombreOperacion = rs.getString("Nombre");
                        int limiteTareas = rs.getInt("Limite");
                        String tareas = rs.getString("Tareas");
                        Operacion operacion = new Operacion(idOperacion, nombreOperacion, limiteTareas, tareas);
                        operaciones.add(operacion);
                    }
                }
            } catch (SQLException e) {
                alerta = new Alert(Alert.AlertType.ERROR, "Error al realizar la consulta: " + e.getMessage());
                alerta.showAndWait();
            }
        }
        return operaciones;
    }
}
