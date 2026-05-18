package com.proyectofinal.billeteravirtual.response;

import com.proyectofinal.billeteravirtual.enums.NivelUsuario;
import com.proyectofinal.billeteravirtual.model.Transaccion;

import java.util.ArrayList;

public class UsuarioResponse {

    private String nombreCompleto;
    private String cedula;
    private int puntos;
    private NivelUsuario nivel;
    private String correoElectronico;
    private String numeroTelefonico;
    private double saldoTotal;
    private double transaccionesTotal;

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public NivelUsuario getNivel() {
        return nivel;
    }

    public void setNivel(NivelUsuario nivel) {
        this.nivel = nivel;
    }

    public double getSaldoTotal() {
        return saldoTotal;
    }

    public void setSaldoTotal(double saldoTotal) {
        this.saldoTotal = saldoTotal;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getNumeroTelefonico() {
        return numeroTelefonico;
    }

    public void setNumeroTelefonico(String numeroTelefonico) {
        this.numeroTelefonico = numeroTelefonico;
    }

    public double getTransaccionesTotal() {
        return transaccionesTotal;
    }

    public void setTransaccionesTotal(double transaccionesTotal) {
        this.transaccionesTotal = transaccionesTotal;
    }
}