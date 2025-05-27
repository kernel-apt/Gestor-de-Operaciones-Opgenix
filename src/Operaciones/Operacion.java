/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Operaciones;

/**
 *
 * @author parca
 */
public class Operacion {

    private int id;

    private String nombreOperacion;
    private int numeroTareas;
    private String tareasAsociadas;

    public Operacion(int id, String nombreOperacion, int numeroTareas, String tareasAsociadas) {
        this.id = id;
        this.nombreOperacion = nombreOperacion;
        this.numeroTareas = numeroTareas;
        this.tareasAsociadas = tareasAsociadas;
    }

    public Operacion(String nombreOperacion, int numeroTareas, String tareasAsociadas) {
        this.nombreOperacion = nombreOperacion;
        this.numeroTareas = numeroTareas;
        this.tareasAsociadas = tareasAsociadas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getTareasAsociadas() {
        return tareasAsociadas;
    }

    public void setTareasAsociadas(String tareasAsociadas) {
        this.tareasAsociadas = tareasAsociadas;
    }

}
