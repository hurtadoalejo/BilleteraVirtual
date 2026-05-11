package com.proyectofinal.billeteravirtual.model;

import java.util.ArrayList;

public class UsuarioResponse {

    private String nombreCompleto;
    private String cedula;
    private int puntos;
    private NivelUsuario nivel;

    private double saldoTotal;

    private ArrayList<Transaccion> historialTransacciones;

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

    public ArrayList<Transaccion> getHistorialTransacciones() {
        return historialTransacciones;
    }

    public void setHistorialTransacciones(ArrayList<Transaccion> historialTransacciones) {
        this.historialTransacciones = historialTransacciones;
    }
}