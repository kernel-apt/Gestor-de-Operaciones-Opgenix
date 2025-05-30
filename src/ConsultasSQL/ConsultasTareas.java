package ConsultasSQL;

import Tareas.Tareas;
import gestorDeOperaciones.GestorDeOperaciones;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;

public class ConsultasTareas {

    Connection con = GestorDeOperaciones.getConnection();
    private List<String> cadenaTareas = new ArrayList<>();
    private List<Integer> cantidades = new ArrayList<>();

    public ArrayList<String> ListaTareas() {
        cadenaTareas.clear();
        try {
            if (con != null) {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT Nombre FROM tarea");
                while (rs.next()) {
                    cadenaTareas.add(rs.getString("Nombre"));
                }
                rs.close();
                st.close();
            }
        } catch (SQLException e) {
            mostrarAlerta("No se pudo obtener la lista de tareas.",
                    "Verifica que la base de datos esté activa y que la tabla 'tarea' exista.",
                    e);
        }
        return (ArrayList<String>) cadenaTareas;
    }

    public ArrayList<Integer> ListaTareasCantidad() {
        if (cantidades == null) {
            cantidades = new ArrayList<>();
        }
        cantidades.clear();

        cantidades.add(0);
        cantidades.add(0);
        cantidades.add(0);

        try {
            if (con != null) {
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
            }
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
            if (con != null) {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM tarea");

                while (rs.next()) {
                    Tareas tarea = new Tareas(
                            rs.getInt("idTarea"),
                            rs.getString("Nombre"),
                            rs.getString("Descripcion"),
                            rs.getBoolean("Pausa"),
                            rs.getBoolean("Reanudar"),
                            rs.getBoolean("Reiniciar"),
                            rs.getString("Dependencia"),
                            rs.getString("Instruccion")
                    );
                    listaTareas.add(tarea);
                }

                rs.close();
                st.close();
            }
        } catch (SQLException e) {
            mostrarAlerta("No se pudo obtener la lista completa de tareas.",
                    "Es posible que la tabla 'tarea' no esté correctamente definida.",
                    e);
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
                            Tareas tarea = new Tareas(
                                    rs.getInt("idTarea"),
                                    rs.getString("Nombre"),
                                    rs.getString("Descripcion"),
                                    rs.getBoolean("Pausa"),
                                    rs.getBoolean("Reanudar"),
                                    rs.getBoolean("Reiniciar"),
                                    rs.getString("Dependencia"),
                                    rs.getString("Instruccion")
                            );
                            listaTareas.add(tarea);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("No se pudo filtrar la tarea seleccionada.",
                    "Verifica que el nombre ingresado exista en la base de datos.",
                    e);
        }
        return listaTareas;
    }

    public boolean Guardar(Tareas tarea) {
        boolean creado = false;
        if (con != null) {
            String sql = "INSERT INTO tarea (Nombre, Descripcion, Pausa, Reanudar, Reiniciar, Dependencia, Instruccion, Estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tarea.getNombreTarea());
                ps.setString(2, tarea.getDescripcion());
                ps.setBoolean(3, tarea.getValorPausa());
                ps.setBoolean(4, tarea.getValorReanudar());
                ps.setBoolean(5, tarea.getValorReiniciar());
                ps.setString(6, tarea.getDependencia());
                ps.setString(7, tarea.getInstruccion());
                ps.setString(8, "Creado");

                creado = ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta("No se pudo guardar la tarea.",
                        "Es posible que ya exista una tarea con el mismo nombre o haya un error en los datos.",
                        e);
            }
        }
        return creado;
    }

    public boolean Modificar(Tareas tarea, int id) {
        boolean modificado = false;
        if (con != null) {
            String sql = "UPDATE tarea SET Nombre = ?, Descripcion = ?, Pausa = ?, Reanudar = ?, Reiniciar = ?, Dependencia = ?, Instruccion = ?, Estado = ? WHERE idTarea = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, tarea.getNombreTarea());
                ps.setString(2, tarea.getDescripcion());
                ps.setBoolean(3, tarea.getValorPausa());
                ps.setBoolean(4, tarea.getValorReanudar());
                ps.setBoolean(5, tarea.getValorReiniciar());
                ps.setString(6, tarea.getDependencia());
                ps.setString(7, tarea.getInstruccion());
                ps.setString(8, "Modificado");
                ps.setInt(9, id);

                modificado = ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta("No se pudo actualizar la tarea.",
                        "Verifica que el ID sea válido y que los datos estén correctamente ingresados.",
                        e);
            }
        }
        return modificado;
    }

    public boolean Eliminar(int id) {
        boolean eliminado = false;
        if (con != null) {
            String sql = "DELETE FROM tarea WHERE idTarea = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                eliminado = ps.executeUpdate() > 0;
            } catch (SQLException e) {
                mostrarAlerta("No se pudo eliminar la tarea.",
                        "Es posible que la tarea esté relacionada con una operación activa.",
                        e);
            }
        }
        return eliminado;
    }

    // Alerta refinada con detalles técnicos opcionales
    private void mostrarAlerta(String mensaje, String sugerencia, Exception e) {
        Alert alerta = new Alert(AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(mensaje);
        alerta.setContentText(sugerencia);

        String detallesTecnicos = e.toString();
        TextArea textArea = new TextArea(detallesTecnicos);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alerta.getDialogPane().setExpandableContent(textArea);
        alerta.getDialogPane().setExpanded(false);

        alerta.showAndWait();
    }
}
