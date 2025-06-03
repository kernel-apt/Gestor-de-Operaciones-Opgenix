package ConsultasSQL;

import Objetos.Operacion;
import gestorDeOperaciones.GestorDeOperaciones;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;

public class ConsultasOperaciones {
    private Connection con;

    public ConsultasOperaciones(Connection con) {
        this.con = con;
    }
    
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
        ArrayList<String> listaTareas = new ArrayList<>();
        if (con != null) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT Nombre FROM tarea")) {
                while (rs.next()) {
                    listaTareas.add(rs.getString("Nombre"));
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "Error al obtener la lista de tareas.");
            }
        }
        return listaTareas;
    }

    public ArrayList<String[]> ListaOperaciones() {
        ArrayList<String[]> listaOperaciones = new ArrayList<>();
        if (con != null) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT Nombre, Estado FROM operacion")) { // CORREGIDO
                while (rs.next()) {
                    String nombreOperacion = rs.getString("Nombre");
                    String estado = rs.getString("Estado");
                    listaOperaciones.add(new String[]{nombreOperacion, estado});
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "Error al obtener la lista de operaciones.");
            }
        }
        return listaOperaciones;
    }
    
    
    public List<String> buscarTareasPorOperacion(String nombreOperacion) {
        List<String> listaTareas = new ArrayList<>();
        String sql = "SELECT Nombre FROM tarea WHERE NombreOperacion = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreOperacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaTareas.add(rs.getString("Nombre"));
                }
            }
        } catch (SQLException e) {
            mostrarAlerta(e, "Error al buscar tareas por operación.");
        }
        return listaTareas;
    }

    public boolean Guardar(Operacion operacion) {
        boolean creado = false;
        if (con != null) {
            String sql = "INSERT INTO operacion (Nombre, Limite, Salida, Estado) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, operacion.getNombreOperacion());
                ps.setInt(2, operacion.getNumeroTareas());
                ps.setString(3, operacion.getSalida());
                ps.setString(4, "No ejecutada");
                creado = ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo guardar la operación.");
            }
        }
        return creado;
    }

    
    public boolean Modificar(Operacion operacion) {
        boolean modificado = false;
        if (con != null) {
            String sql = "UPDATE operacion SET Limite = ?, Salida = ? WHERE Nombre = ?"; // CORREGIDO
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, operacion.getNumeroTareas());
                ps.setString(2, operacion.getSalida());
                ps.setString(3, operacion.getNombreOperacion());
                modificado = ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo modificar la operación.");
            }
        }
        return modificado;
    }

    public boolean actualizarEstadoOperacion(String nombreOperacion, String nuevoEstado) {
        boolean actualizado = false;
        if (con != null) {
            String sqlUpdateOperacion = "UPDATE operacion SET Estado = ? WHERE Nombre = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateOperacion)) {
                ps.setString(1, nuevoEstado);
                ps.setString(2, nombreOperacion);
                actualizado = ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo actualizar el estado de la operación.");
                return false;
            }

            // Actualizar también las tareas relacionadas (según campo NombreOperacion)
            String sqlUpdateTarea = "UPDATE tarea SET Estado = ? WHERE NombreOperacion = ?";
            try (PreparedStatement psTarea = con.prepareStatement(sqlUpdateTarea)) {
                psTarea.setString(1, nuevoEstado);
                psTarea.setString(2, nombreOperacion);
                psTarea.executeUpdate();
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo actualizar el estado de las tareas asociadas.");
            }
        }
        return actualizado;
    }

    public boolean Eliminar(String nombreOperacion) {
        boolean eliminado = false;
        if (con != null) {
            String sql = "DELETE FROM operacion WHERE Nombre = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombreOperacion);
                eliminado = ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo eliminar la operación. Asegúrate de que no existan tareas relacionadas.");
            }
        }
        return eliminado;
    }

    public List<Operacion> ConsultaOperacion() {
        List<Operacion> operaciones = new ArrayList<>();
        if (con != null) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM operacion")) {
                while (rs.next()) {
                    operaciones.add(new Operacion(
                            rs.getString("Nombre"),
                            rs.getInt("Limite"),
                            rs.getString("Salida")
                    ));
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
                        operaciones.add(new Operacion(
                                rs.getString("Nombre"),
                                rs.getInt("Limite"),
                                rs.getString("Estado"),
                                rs.getString("Salida")
                                
                        ));
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
            String sql = "SELECT * FROM operacion WHERE Nombre = ? AND Estado = 'En ejecucion'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, consultaOperacion);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        operaciones.add(new Operacion(
                                rs.getString("Nombre"),
                                rs.getInt("Limite"),
                                rs.getString("Salida")
                        ));
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo consultar la operación activa.");
            }
        }
        return operaciones;
    }
    
    public int ConsultaOperacionEnEjecucion() {
    int total = 0;
    if (con != null) {
        String sql = "SELECT COUNT(*) FROM operacion WHERE Estado = 'En ejecucion'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                total = rs.getInt(1);  // Obtener el conteo
            }
            
        } catch (SQLException e) {
            mostrarAlerta(e, "No se pudo consultar la operación activa.");
        }
    }
    return total;
}

}
