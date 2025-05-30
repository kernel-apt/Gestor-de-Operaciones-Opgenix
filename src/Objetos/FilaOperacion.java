/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Objetos;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author parca
 */
public class FilaOperacion {
    private final SimpleStringProperty operacion;

    public FilaOperacion(String operacion) {
        this.operacion = new SimpleStringProperty(operacion);
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
}

