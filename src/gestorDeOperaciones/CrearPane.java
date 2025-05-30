/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestorDeOperaciones;

/**
 *
 * @author parca
 */
public class CrearPane {
 private String ruta= null;
    public String Abrir(String id) {    
        switch (id) {
            case "CrearOperacion":
            case "EditorOperacion":
                ruta = "/FXML/" + id + ".fxml";
                break;
            case "CrearTarea":
            case "EditarTarea":
                ruta = "/FXML/" + id + ".fxml";
                break;
            case "AcercaDe":
                ruta = "/FXML/AcercaDe.fxml";
                break;
            default:
                ruta= "Acción no reconocida: " + id;
                return ruta;
        }
        return ruta;

    }
}
