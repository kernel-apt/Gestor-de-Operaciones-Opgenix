package gestorDeOperaciones;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import gestorDeOperaciones.CrearPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class PantallaPrincipalController {
    private String text;
    
    @FXML
    private AnchorPane ap_Operaciones;

    @FXML
    private AnchorPane ap_Principal;

    @FXML
    private AnchorPane ap_TareasDeOperaciones;

    @FXML
    private ScrollPane scp_ProcesoOperaciones;

    @FXML
    private TextField tf_OperacionesActivas;

    @FXML
    private TextField tf_TareasEjecucion;

    @FXML
    private TextField tf_TotalOperaciones;

    @FXML
    public void Menu(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        String id = item.getId();
        System.out.println("\n El id es: " + id);

        CrearPane crear = new CrearPane();
        String text = crear.Abrir(id);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(text));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Crear un nuevo Stage (ventana)
            Stage newStage = new Stage();
            newStage.setTitle("Nueva Ventana");
            newStage.setScene(scene);

            // Opcional: establecer que esta ventana sea modal (bloquea la ventana anterior)
            // newStage.initModality(Modality.APPLICATION_MODAL);
            // Mostrar la nueva ventana
            newStage.show();

        } catch (Exception e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo cargar la pantalla principal");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }



}
