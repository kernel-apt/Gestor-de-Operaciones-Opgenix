/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Objetos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author parca
 */
public class Tareas {


    private String nombreTarea;
    private String descripcion;
    private Boolean valorPausa;
    private Boolean valorReanudar;
    private Boolean valorReiniciar;
    private String Dependencia;
    private String Salida;
    private String Estado;

    public Tareas(String nombreTarea, String descripcion, Boolean valorPausa, Boolean valorReanudar, Boolean valorReiniciar, String Dependencia, String Salida) {
        this.nombreTarea = nombreTarea;
        this.descripcion = descripcion;
        this.valorPausa = valorPausa;
        this.valorReanudar = valorReanudar;
        this.valorReiniciar = valorReiniciar;
        this.Dependencia = Dependencia;
        this.Salida = Salida;
    }

    public Tareas(String nombreTarea, String descripcion, Boolean valorPausa, Boolean valorReanudar, Boolean valorReiniciar, String Dependencia, String Salida, String Estado) {
        this.nombreTarea = nombreTarea;
        this.descripcion = descripcion;
        this.valorPausa = valorPausa;
        this.valorReanudar = valorReanudar;
        this.valorReiniciar = valorReiniciar;
        this.Dependencia = Dependencia;
        this.Salida = Salida;
        this.Estado = Estado;
    }

    
    
    
    public String getEstado() {
        return Estado;
    }

    public void setEstado(String Estado) {
        this.Estado = Estado;
    }

    
    
    
    public String getSalida() {
        return Salida;
    }

    public void setSalida(String Salida) {
        this.Salida = Salida;
    }
    


    public String getDependencia() {
        return Dependencia;
    }

    public void setDependencia(String Dependencia) {
        this.Dependencia = Dependencia;
    }

 
   
    public String getNombreTarea() {
        return nombreTarea;
    }

    public void setNombreTarea(String nombreTarea) {
        this.nombreTarea = nombreTarea;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getValorPausa() {
        return valorPausa;
    }

    public void setValorPausa(Boolean valorPausa) {
        this.valorPausa = valorPausa;
    }

    public Boolean getValorReanudar() {
        return valorReanudar;
    }

    public void setValorReanudar(Boolean valorReanudar) {
        this.valorReanudar = valorReanudar;
    }

    public Boolean getValorReiniciar() {
        return valorReiniciar;
    }

    public void setValorReiniciar(Boolean valorReiniciar) {
        this.valorReiniciar = valorReiniciar;
    }

    
    
    
        
}
