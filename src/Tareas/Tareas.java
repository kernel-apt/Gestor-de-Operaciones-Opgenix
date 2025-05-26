/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Tareas;

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
    private String Instruccion;

    public Tareas(String nombreTarea, String descripcion, Boolean valorPausa, Boolean valorReanudar, Boolean valorReiniciar, String Dependencia, String Instruccion) {
        this.nombreTarea = nombreTarea;
        this.descripcion = descripcion;
        this.valorPausa = valorPausa;
        this.valorReanudar = valorReanudar;
        this.valorReiniciar = valorReiniciar;
        this.Dependencia = Dependencia;
        this.Instruccion = Instruccion;
    }

    public String getDependencia() {
        return Dependencia;
    }

    public void setDependencia(String Dependencia) {
        this.Dependencia = Dependencia;
    }

    public String getInstruccion() {
        return Instruccion;
    }

    public void setInstruccion(String Instruccion) {
        this.Instruccion = Instruccion;
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
