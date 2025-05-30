package ConsultasSQL;

import Objetos.Operacion;
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

public class ConsultasOperaciones {

    Connection con = GestorDeOperaciones.getConnection();
    private List<String> cadenaOperacion = new ArrayList<>();
    Alert alerta;

    private void mostrarAlerta(SQLException e, String mensajeBase) {
        String mensajeAmigable;
        String codigoSQL = e.getSQLState();

        switch (codigoSQL) {
            case "23505":
                mensajeAmigable = "El nombre ya está en uso. Usa uno diferente.";
                break;
            case "23503":
                mensajeAmigable = "Una tarea asociada no existe o no es válida.";
                break;
            case "08001":
                mensajeAmigable = "No se pudo conectar a la base de datos. Verifica tu conexión.";
                break;
            default:
                mensajeAmigable = mensajeBase;
        }

        alerta = new Alert(Alert.AlertType.ERROR, mensajeAmigable + "\n\nDetalles técnicos: " + e.getMessage());
        alerta.showAndWait();
    }

    public ArrayList<String> ListaTareas() {
        cadenaOperacion.clear();
        if (con != null) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT Nombre FROM tarea")) {
                while (rs.next()) {
                    String nombreTarea = rs.getString("Nombre");
                    cadenaOperacion.add(nombreTarea);
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "Error al obtener la lista de tareas.");
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
                mostrarAlerta(e, "Error al obtener la lista de operaciones.");
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
                creado = filasInsertadas != 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo guardar la operación.");
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
                modificado = filasActualizadas != 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo modificar la operación.");
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

                        String sqlUpdateOperacion = "UPDATE operacion SET Estado = ? WHERE idOperacion = ?";
                        try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateOperacion)) {
                            psUpdate.setString(1, "En ejecucion");
                            psUpdate.setInt(2, idOperacion);
                            actualizado = psUpdate.executeUpdate() > 0;

                        }

                        if (tareasAsociadas != null && !tareasAsociadas.trim().isEmpty()) {
                            String[] tareas = tareasAsociadas.split(",");
                            String sqlUpdateTarea = "UPDATE tarea SET Estado = ? WHERE Nombre = ?";

                            try (PreparedStatement psUpdateTarea = con.prepareStatement(sqlUpdateTarea)) {

                                for (String tarea : tareas) {

                                    psUpdateTarea.setString(1, "En ejecucion");
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
                mostrarAlerta(e, "No se pudo activar la operación.");
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

                        String sqlUpdateOperacion = "UPDATE operacion SET Estado = ? WHERE idOperacion = ?";
                        try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdateOperacion)) {
                            psUpdate.setString(1, "Creado");
                            psUpdate.setInt(2, idOperacion);
                            actualizado = psUpdate.executeUpdate() > 0;
                        }

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
                mostrarAlerta(e, "No se pudo detener la operación.");
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
                eliminado = ps.executeUpdate() != 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo eliminar la operación.");
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
                    operaciones.add(new Operacion(idOperacion, nombreOperacion, limiteTareas, tareas));
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudieron consultar las operaciones.");
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
                        operaciones.add(new Operacion(idOperacion, nombreOperacion, limiteTareas, tareas));
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo consultar la operación.");
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
                        operaciones.add(new Operacion(idOperacion, nombreOperacion, limiteTareas, tareas));
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo consultar la operación activa.");
            }
        }
        return operaciones;
    }
}
