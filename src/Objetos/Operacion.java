/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Objetos;

/**
 *
 * @author parca
 */
public class Operacion {


    private String nombreOperacion;
    private int numeroTareas;
    private String estado;
    private String salida;

    public Operacion(String nombreOperacion, int numeroTareas, String estado, String salida) {
        this.nombreOperacion = nombreOperacion;
        this.numeroTareas = numeroTareas;
        this.estado = estado;
        this.salida = salida;
    }

    
    
    
    public Operacion(String nombreOperacion, int numeroTareas, String salida) {
        this.nombreOperacion = nombreOperacion;
        this.numeroTareas = numeroTareas;
        this.salida = salida;
    }

    public String getSalida() {
        return salida;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }


    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreOperacion() {
        return nombreOperacion;
    }

    public void setNombreOperacion(String nombreOperacion) {
        this.nombreOperacion = nombreOperacion;
    }

    public int getNumeroTareas() {
        return numeroTareas;
    }

    public void setNumeroTareas(int numeroTareas) {
        this.numeroTareas = numeroTareas;
    }


}
