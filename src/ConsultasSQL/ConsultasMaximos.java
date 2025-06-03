/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ConsultasSQL;

import Objetos.Maximos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import java.sql.ResultSet;

/**
 *
 * @author parca
 */
public class ConsultasMaximos {

    private Connection con;

    public ConsultasMaximos(Connection con) {
        this.con = con;
    }
    Alert alerta;

    public void crearMaximos() {

        String consulta = "SELECT COUNT(*) FROM maximos WHERE idMaximos = ?";
        String insercion = "INSERT INTO maximos (idMaximos, MaximoEjecucion, MaximoCreacion) VALUES (?, 10, 10)";
        String idMaximos = "max1";
        try (PreparedStatement psConsulta = con.prepareStatement(consulta)) {
            psConsulta.setString(1, idMaximos);
            ResultSet rs = psConsulta.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count == 0) {
                    try (PreparedStatement psInsert = con.prepareStatement(insercion)) {
                        psInsert.setString(1, idMaximos);
                        psInsert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar o crear registro en maximos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
     public Maximos obtenerMaximos() {
         Maximos max=null;
        String sql = "SELECT MaximoEjecucion, MaximoCreacion FROM maximos ";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int maxEjec = rs.getInt("MaximoEjecucion");
                    int maxCreac = rs.getInt("MaximoCreacion");
                    max= new Maximos(maxEjec, maxCreac);
                } 
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener maximos: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    return max;
}
     public boolean actualizarMaximoEjecucion( int nuevoMaximoEjecucion) {
        String sql = "UPDATE maximos SET MaximoEjecucion = ? WHERE idMaximos = 'max1'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoMaximoEjecucion);
            int filasActualizadas = ps.executeUpdate();
            return filasActualizadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar MaximoEjecucion: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
     
     public boolean actualizarMaximoCreacion( int nuevoMaximoCreacion) {
        String sql = "UPDATE maximos SET MaximoCreacion = ? WHERE idMaximos = 'max1'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, nuevoMaximoCreacion);
            int filasActualizadas = ps.executeUpdate();
            return filasActualizadas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar MaximoCreacion: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
