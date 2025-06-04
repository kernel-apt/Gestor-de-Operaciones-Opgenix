package ConsultasSQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;

public class ConsultaInstrucciones {

    private Connection con;

    public ConsultaInstrucciones(Connection con) {
        this.con = con;

    }
    Alert alerta;

    private void mostrarAlerta(SQLException e, String mensajeBase) {
        String mensajeAmigable;
        String codigoSQL = e.getSQLState();

        switch (codigoSQL) {
            case "23505":
                mensajeAmigable = "La instrucción ya existe.";
                break;
            case "23503":
                mensajeAmigable = "La tarea asociada no existe.";
                break;
            case "08001":
                mensajeAmigable = "No se pudo conectar a la base de datos.";
                break;
            default:
                mensajeAmigable = mensajeBase;
        }

        alerta = new Alert(Alert.AlertType.ERROR, mensajeAmigable + "\n\nDetalles técnicos: " + e.getMessage());
        alerta.showAndWait();
    }

    private boolean existeTarea(String nombreTarea) {
        if (con != null) {
            String sql = "SELECT 1 FROM tarea WHERE Nombre = ? FETCH FIRST 1 ROW ONLY";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombreTarea);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "Error al verificar la existencia de la tarea.");
            }
        }
        return false;
    }

    public boolean crearInstruccion(String nombreInstruccion, String nombreTarea) {
        if (con != null) {
            if (!existeTarea(nombreTarea)) {
                alerta = new Alert(Alert.AlertType.ERROR, "No se puede crear la instrucción. La tarea asociada no existe.");
                alerta.showAndWait();
                return false;
            }

            boolean yaExiste = instruccionExisteParaTarea(nombreInstruccion, nombreTarea);
            if (!yaExiste) {
                String sql = "INSERT INTO instruccion (Nombre, nombreTarea, Estado) VALUES (?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, nombreInstruccion);
                    ps.setString(2, nombreTarea);
                    ps.setString(3, "No ejecutada");
                    return ps.executeUpdate() > 0;
                } catch (SQLException e) {
                    mostrarAlerta(e, "No se pudo crear la instrucción.");
                }
            }
        }
        return false;
    }

    public boolean instruccionExisteParaTarea(String nombreInstruccion, String nombreTarea) {
        String sql = "SELECT COUNT(*) FROM instruccion WHERE Nombre = ? AND nombreTarea = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, nombreInstruccion);
            stmt.setString(2, nombreTarea);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de instrucción: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean cambiarEstadoInstruccion(String nombreInstruccion, String nuevoEstado) {
        if (con != null) {
            String sql = "UPDATE instruccion SET Estado = ? WHERE Nombre = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setString(2, nombreInstruccion);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo cambiar el estado de la instrucción.");
            }
        }
        return false;
    }
    
    public boolean cambiarEstadoInstruccionPorTarea(String tarea, String nombreInstruccion, String nuevoEstado) {
        if (con != null) {
            String sql = "UPDATE instruccion SET Estado = ? WHERE Nombre = ? AND nombreTarea = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setString(2, nombreInstruccion);
                ps.setString(3, tarea);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo cambiar el estado de la instrucción.");
            }
        }
        return false;
    }

    public boolean estaInstruccionCompletada(String tarea, String instruccion) {
        if (con != null) {
            String sql = "SELECT Estado FROM instruccion WHERE nombreTarea = ? AND Nombre = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tarea);
                ps.setString(2, instruccion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return "Completado".equalsIgnoreCase(rs.getString("Estado"));
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo verificar el estado de la instrucción.");
            }
        }
        return false;
    }

    public boolean estanTodasCompletadas(String tarea) {
        if (con != null) {
            String sql = "SELECT COUNT(*) FROM instruccion WHERE nombreTarea = ? AND Estado <> 'Completado'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tarea);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int pendientes = rs.getInt(1);
                        return pendientes == 0;
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo verificar el estado de las instrucciones.");
            }
        }
        return false;
    }

    public int obtenerPorcentajeCompletado(String tarea) {
        if (con != null) {
            String sqlTotal = "SELECT COUNT(*) FROM instruccion WHERE nombreTarea = ?";
            String sqlCompletadas = "SELECT COUNT(*) FROM instruccion WHERE nombreTarea = ? AND Estado = 'Completado'";
            try (
                    PreparedStatement psTotal = con.prepareStatement(sqlTotal); PreparedStatement psCompletadas = con.prepareStatement(sqlCompletadas)) {
                psTotal.setString(1, tarea);
                psCompletadas.setString(1, tarea);

                int total = 0;
                int completadas = 0;

                try (ResultSet rsTotal = psTotal.executeQuery()) {
                    if (rsTotal.next()) {
                        total = rsTotal.getInt(1);
                    }
                }

                try (ResultSet rsCompletadas = psCompletadas.executeQuery()) {
                    if (rsCompletadas.next()) {
                        completadas = rsCompletadas.getInt(1);
                    }
                }

                if (total > 0) {
                    return (int) ((completadas * 100.0f) / total);
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudo calcular el porcentaje de instrucciones completadas.");
            }
        }
        return 0;
    }

    public List<String> instruccionesAsociadas(String tarea) {
        List<String> listaInstrucciones = new ArrayList<>();
        if (con != null) {
            String sql = "SELECT Nombre FROM instruccion WHERE nombreTarea = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tarea);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        listaInstrucciones.add(rs.getString("Nombre"));
                    }
                }
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudieron obtener las instrucciones asociadas.");
            }
        }
        return listaInstrucciones;
    }

    public boolean eliminarInstruccionesPorTarea(String tarea) {
        if (con != null) {
            String sql = "DELETE FROM instruccion WHERE nombreTarea = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tarea);
                int filasEliminadas = ps.executeUpdate();
                return filasEliminadas > 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudieron eliminar las instrucciones de la tarea.");
            }
        }
        return false;
    }
    
    
    public boolean eliminarInstruccionEspecifica(String tarea, String instruccion) {
        if (con != null) {
            String sql = "DELETE FROM instruccion WHERE nombreTarea = ? AND Nombre = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tarea);
                ps.setString(2, instruccion);
                int filasEliminadas = ps.executeUpdate();
                return filasEliminadas > 0;
            } catch (SQLException e) {
                mostrarAlerta(e, "No se pudieron eliminar las instrucciones de la tarea.");
            }
        }
        return false;
    }
}
