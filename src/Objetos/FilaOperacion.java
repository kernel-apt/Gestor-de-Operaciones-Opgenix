package Objetos;

import javafx.beans.property.SimpleStringProperty;

public class FilaOperacion {
    private final SimpleStringProperty operacion;
    private final SimpleStringProperty estado;

    public FilaOperacion(String operacion) {
        this.operacion = new SimpleStringProperty(operacion);
        this.estado = new SimpleStringProperty(""); 
    }

    public FilaOperacion(SimpleStringProperty operacion, String estado) {
        this.operacion = operacion;
        this.estado = new SimpleStringProperty(estado);
    }

    public FilaOperacion(String operacion, String estado) {
        this.operacion = new SimpleStringProperty(operacion);
        this.estado = new SimpleStringProperty(estado);
    }

    public String getOperacion() {
        return operacion.get();
    }

    public void setOperacion(String operacion) {
        this.operacion.set(operacion);
    }

    public SimpleStringProperty operacionProperty() {
        return operacion;
    }

    // Getter y setter de "estado"
    public String getEstado() {
        return estado.get();
    }

    public void setEstado(String estado) {
        this.estado.set(estado);
    }

    public SimpleStringProperty estadoProperty() {
        return estado;
    }
}
