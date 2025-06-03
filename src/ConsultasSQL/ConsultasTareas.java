package ConsultasSQL;

import Objetos.Tareas;
import gestorDeOperaciones.GestorDeOperaciones;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public class ConsultasTareas {

    private Connection con;

    public ConsultasTareas(Connection con) {
        this.con = con;
    }

    private List<String> cadenaTareas = new ArrayList<>();
    private List<Integer> cantidades = new ArrayList<>();

    public ArrayList<String> ListaTareas() {
        cadenaTareas.clear();
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT Nombre FROM tarea");
            while (rs.next()) {
                cadenaTareas.add(rs.getString("Nombre"));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            mostrarAlerta("No se pudo obtener la lista de tareas.",
                    "Verifica que la base de datos esté activa y que la tabla 'tarea' exista.",
                    e);
        }
        return (ArrayList<String>) cadenaTareas;
    }

    public ArrayList<String> ListaTareasSinOperacion() {
        cadenaTareas.clear();
        try {
            Statement st = con.createStatement();
            // Consulta modificada para filtrar solo tareas con NombreOperacion NULL
            ResultSet rs = st.executeQuery("SELECT Nombre FROM tarea WHERE NombreOperacion IS NULL");
            while (rs.next()) {
                cadenaTareas.add(rs.getString("Nombre"));
            }
            rs.close();
            st.close();
        } catch (SQLException e) {
            mostrarAlerta("No se pudo obtener la lista de tareas sin operación.",
                    "Verifica que la base de datos esté activa y que la tabla 'tarea' exista.",
                    e);
        }
        return (ArrayList<String>) cadenaTareas;
    }

    public boolean estanTodasTareasFinalizadas(String operacion) {
        if (con != null) {
            String sql = "SELECT COUNT(*) FROM tarea WHERE NombreOperacion = ? AND Estado <> 'Finalizada'";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, operacion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int pendientes = rs.getInt(1);
                        // Si no hay tareas pendientes (Estado diferente a Finalizada), entonces todas están finalizadas
                        return pendientes == 0;
                    }
                }
            } catch (SQLException e) {
                // Aquí puedes manejar el error, lanzar excepción o imprimir mensaje
                e.printStackTrace();
            }
        }
        // Si no hay conexión o hubo error, retornamos false
        return false;
    }

    public int obtenerPorcentajeTareasFinalizadas(String operacion) {
        if (con != null) {
            String sqlTotal = "SELECT COUNT(*) FROM tarea WHERE NombreOperacion = ?";
            String sqlFinalizadas = "SELECT COUNT(*) FROM tarea WHERE NombreOperacion = ? AND Estado = 'Finalizada'";

            try (
                    PreparedStatement psTotal = con.prepareStatement(sqlTotal); PreparedStatement psFinalizadas = con.prepareStatement(sqlFinalizadas)) {
                // Total de tareas
                psTotal.setString(1, operacion);
                ResultSet rsTotal = psTotal.executeQuery();
                int total = 0;
                if (rsTotal.next()) {
                    total = rsTotal.getInt(1);
                }

                // Total de tareas finalizadas
                psFinalizadas.setString(1, operacion);
                ResultSet rsFinalizadas = psFinalizadas.executeQuery();
                int finalizadas = 0;
                if (rsFinalizadas.next()) {
                    finalizadas = rsFinalizadas.getInt(1);
                }

                if (total > 0) {
                    return (int) ((finalizadas * 100.0) / total);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 1000; // En caso de error o si no hay tareas
    }

    public ArrayList<Integer> ListaTareasCantidad() {
        cantidades.clear();
        cantidades.add(0); // Total de operaciones
        cantidades.add(0); // Operaciones en ejecución
        cantidades.add(0); // Tareas en ejecución

        try {
            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM operacion");
            if (rs.next()) {
                cantidades.set(0, rs.getInt(1));
            }
            rs.close();

            rs = st.executeQuery("SELECT COUNT(*) FROM operacion WHERE Estado = 'En ejecucion'");
            if (rs.next()) {
                cantidades.set(1, rs.getInt(1));
            }
            rs.close();

            rs = st.executeQuery("SELECT COUNT(*) FROM tarea WHERE Estado = 'En ejecucion'");
            if (rs.next()) {
                cantidades.set(2, rs.getInt(1));
            }
            rs.close();

            st.close();
        } catch (SQLException e) {
            mostrarAlerta("No se pudieron obtener los datos estadísticos.",
                    "Revisa que las tablas 'operacion' y 'tarea' existan y tengan datos.",
                    e);
        }
        return (ArrayList<Integer>) cantidades;
    }

    public List<Tareas> ConsultaTareas() {
        List<Tareas> listaTareas = new ArrayList<>();
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM tarea");

            while (rs.next()) {
                Tareas tarea = new Tareas(
                        rs.getString("Nombre"),
                        rs.getString("Descripcion"),
                        rs.getBoolean("Pausa"),
                        rs.getBoolean("Reanudar"),
                        rs.getBoolean("Reiniciar"),
                        rs.getString("Dependencia"),
                        rs.getString("Salida"),
                        rs.getString("Estado")
                );
                listaTareas.add(tarea);
            }

            rs.close();
            st.close();
        } catch (SQLException e) {
            mostrarAlerta("No se pudo obtener la lista completa de tareas.",
                    "Es posible que la tabla 'tarea' no esté correctamente definida.",
                    e);
        }
        return listaTareas;
    }

    public Tareas ConsultaTareas(String nombreTareaFiltro) {
        Tareas tarea = null;
        String sql = "SELECT * FROM tarea WHERE Nombre = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreTareaFiltro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tarea = new Tareas(
                            rs.getString("Nombre"),
                            rs.getString("Descripcion"),
                            rs.getBoolean("Pausa"),
                            rs.getBoolean("Reanudar"),
                            rs.getBoolean("Reiniciar"),
                            rs.getString("Dependencia"),
                            rs.getString("Salida"),
                            rs.getString("Estado")
                    );
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("No se pudo filtrar la tarea seleccionada.",
                    "Verifica que el nombre ingresado exista en la base de datos.",
                    e);
        }
        return tarea;
    }

    public boolean Guardar(Tareas tarea) {
        boolean creado = false;
        String sql = "INSERT INTO tarea (Nombre, Descripcion, Pausa, Reanudar, Reiniciar, Dependencia, Salida, Estado, NombreOperacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tarea.getNombreTarea());
            ps.setString(2, tarea.getDescripcion());
            ps.setBoolean(3, tarea.getValorPausa());
            ps.setBoolean(4, tarea.getValorReanudar());
            ps.setBoolean(5, tarea.getValorReiniciar());
            ps.setString(6, tarea.getDependencia());
            ps.setString(7, tarea.getSalida());
            ps.setString(8, "No ejecutada");
            ps.setNull(9, Types.VARCHAR);  // No se asigna operación al crear

            creado = ps.executeUpdate() > 0;

            // Hacer commit solo si la conexión está en modo manual
            if (!con.getAutoCommit()) {
                con.commit();
            }

        } catch (SQLException e) {
            try {
                if (!con.getAutoCommit()) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            mostrarAlerta("No se pudo guardar la tarea.",
                    "Es posible que ya exista una tarea con el mismo nombre o haya un error en los datos.",
                    e);
        }
        return creado;
    }

    public boolean Modificar(Tareas tarea) {
        boolean modificado = false;
        String sql = "UPDATE tarea SET Descripcion = ?, Pausa = ?, Reanudar = ?, Reiniciar = ?, Dependencia = ?, Estado = ? WHERE Nombre = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tarea.getDescripcion());
            ps.setBoolean(2, tarea.getValorPausa());
            ps.setBoolean(3, tarea.getValorReanudar());
            ps.setBoolean(4, tarea.getValorReiniciar());
            ps.setString(5, tarea.getDependencia());
            ps.setString(6, "Modificado");
            ps.setString(7, tarea.getNombreTarea());

            modificado = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            mostrarAlerta("No se pudo actualizar la tarea.",
                    "Verifica que los datos estén correctamente ingresados.",
                    e);
        }
        return modificado;
    }

    public List<Tareas> ConsultaTareasPorOperacion(String operacionSeleccionada) {
        List<Tareas> listaTareas = new ArrayList<>();
        String sql = "SELECT * FROM tarea WHERE NombreOperacion = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, operacionSeleccionada);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tareas tarea = new Tareas(
                            rs.getString("Nombre"),
                            rs.getString("Descripcion"),
                            rs.getBoolean("Pausa"),
                            rs.getBoolean("Reanudar"),
                            rs.getBoolean("Reiniciar"),
                            rs.getString("Dependencia"),
                            rs.getString("Salida"),
                            rs.getString("Estado")
                    );
                    listaTareas.add(tarea);
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("No se pudieron obtener las tareas para la operación: " + operacionSeleccionada,
                    "Verifica que la operación exista y que las tareas estén asociadas correctamente.",
                    e);
        }
        return listaTareas;
    }

    public boolean ModificarFK(String tarea, String operacion) {
        boolean modificado = false;
        String sql = "UPDATE tarea SET NombreOperacion = ? WHERE Nombre = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (operacion != null) {
                ps.setString(1, operacion);
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setString(2, tarea);

            modificado = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            mostrarAlerta("No se pudo actualizar la relación tarea-operación.",
                    "Verifica que la operación exista o que el nombre de tarea sea correcto.",
                    e);
        }
        return modificado;
    }

    public String obtenerNombreOperacionPorTarea(String tarea) {
        if (con != null) {
            String sql = "SELECT NombreOperacion FROM tarea WHERE Nombre = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tarea);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("NombreOperacion");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // Retorna null si no se encontró o hubo algún error
        return null;
    }

    public boolean ModificarEstado(String tarea, String estado) {
        boolean modificado = false;
        String sql = "UPDATE tarea SET Estado = ? WHERE Nombre = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, tarea);

            modificado = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            mostrarAlerta("No se pudo actualizar el estado de la tarea.",
                    "Verifica que el nombre de tarea sea correcto.",
                    e);
        }
        return modificado;
    }

    public boolean Eliminar(String nombreTarea) {
        boolean eliminado = false;
        String sql = "DELETE FROM tarea WHERE Nombre = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreTarea);
            eliminado = ps.executeUpdate() > 0;
        } catch (SQLException e) {
            mostrarAlerta("No se pudo eliminar la tarea.",
                    "Asegúrate de que no esté relacionada con alguna instrucción u operación.",
                    e);
        }
        return eliminado;
    }

    private void mostrarAlerta(String mensaje, String sugerencia, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(mensaje);
        alerta.setContentText(sugerencia);

        TextArea textArea = new TextArea(e.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alerta.getDialogPane().setExpandableContent(textArea);
        alerta.getDialogPane().setExpanded(false);

        alerta.showAndWait();
    }
}
