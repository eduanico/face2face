package com.eclipsoft.face2face.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDTO implements Serializable {

    public String apellidos;

    public LocalDate fechaCedulacion;

    @JsonIgnore
    public LocalDate fechaFallecimiento;

    @JsonIgnore
    public LocalDate fechaNacimiento;

    public String fotografia;

    public String nombres;
    private String nombreCompleto;

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaCedulacion() {
        return fechaCedulacion;
    }

    public void setFechaCedulacion(LocalDate fechaCedulacion) {
        this.fechaCedulacion = fechaCedulacion;
    }

    public LocalDate getFechaFallecimiento() {
        return fechaFallecimiento;
    }

    public void setFechaFallecimiento(LocalDate fechaFallecimiento) {
        this.fechaFallecimiento = fechaFallecimiento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getFotografia() {
        return fotografia;
    }

    public void setFotografia(String fotografia) {
        this.fotografia = fotografia;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
}
