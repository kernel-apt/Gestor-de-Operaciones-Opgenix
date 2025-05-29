package gestorDeOperaciones;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GestorDeOperaciones extends Application {

    private static Connection con;

    @Override
    public void start(Stage stage) {
        try {
            conectarBaseDatos();
            inicializarTablas();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("PantallaPrincipal.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setTitle("Inicio");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void conectarBaseDatos() {
        try {
            Class.forName("org.h2.Driver");

            String url = "jdbc:h2:./data/gestiondeoperaciones";

            String user = "sa";
            String password = "";

            con = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión exitosa a la base de datos H2");
        } catch (SQLException e) {
            System.err.println("Error en la conexión a H2: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se pudo cargar el driver de H2");
            e.printStackTrace();
        }
    }

    private void inicializarTablas() {
        String sqlOperacion = "CREATE TABLE IF NOT EXISTS operacion ("
                + "idOperacion INT AUTO_INCREMENT PRIMARY KEY, "
                + "Nombre VARCHAR(45) NOT NULL UNIQUE, "
                + "Limite INT NOT NULL, "
                + "Tareas CLOB, "
                + "Estado VARCHAR(45) NOT NULL)";

        String sqlTarea = "CREATE TABLE IF NOT EXISTS tarea ("
                + "idTarea INT AUTO_INCREMENT PRIMARY KEY, "
                + "Nombre VARCHAR(45) NOT NULL UNIQUE, "
                + "Descripcion VARCHAR(45) NOT NULL, "
                + "Pausa BOOLEAN NOT NULL, "
                + "Reanudar BOOLEAN NOT NULL, "
                + "Reiniciar BOOLEAN NOT NULL, "
                + "Dependencia VARCHAR(45), "
                + "Instruccion VARCHAR(45) NOT NULL, "
                + "Estado VARCHAR(45) NOT NULL)";

        try (Statement stmt = con.createStatement()) {
            stmt.execute(sqlOperacion);
            stmt.execute(sqlTarea);
            System.out.println("Tablas inicializadas o ya existentes.");
        } catch (SQLException e) {
            System.err.println("Error al crear tablas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return con;
    }

    @Override
    public void stop() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
