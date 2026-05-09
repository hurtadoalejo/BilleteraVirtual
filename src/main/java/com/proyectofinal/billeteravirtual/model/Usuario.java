package com.proyectofinal.billeteravirtual.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.proyectofinal.billeteravirtual.util.ArrayList;
import com.proyectofinal.billeteravirtual.util.Stack;

import java.util.HashMap;
import java.util.Map;

public class Usuario {

    private String nombreCompleto;
    private String cedula;
    private String correoElectronico;
    private String numeroTelefonico;
    private String password;

    private int puntos = 0;
    private int puntosAcumulados = 0;
    private NivelUsuario nivel = NivelUsuario.BRONCE;

    private ArrayList<Transaccion> historialTransacciones = new ArrayList<>();
    private ArrayList<TransaccionProgramada>  transaccionesProgramadas = new ArrayList<>();
    private ArrayList<Beneficio> listaBeneficios = new ArrayList<>();
    private Map<String, Billetera> billeteras = new HashMap<>();
    private ArrayList<Notificacion> notificaciones = new ArrayList<>();
    private Stack<Transaccion> pilaReversiones = new Stack<>();

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public void setPuntosAcumulados(int puntosAcumulados) {
        this.puntosAcumulados = puntosAcumulados;
    }

    public NivelUsuario getNivel() {
        return nivel;
    }

    public void setNivel(NivelUsuario nivel) {
        this.nivel = nivel;
    }

    public ArrayList<Transaccion> getHistorialTransacciones() {
        return historialTransacciones;
    }

    public void setHistorialTransacciones(ArrayList<Transaccion> historialTransacciones) {
        this.historialTransacciones = historialTransacciones;
    }

    public ArrayList<Beneficio> getListaBeneficios() {
        return listaBeneficios;
    }

    public void setListaBeneficios(ArrayList<Beneficio> listaBeneficios) {
        this.listaBeneficios = listaBeneficios;
    }

    public Map<String, Billetera> getBilleteras() {
        return billeteras;
    }

    public void setBilleteras(Map<String, Billetera> billeteras) {
        this.billeteras = billeteras;
    }

    public ArrayList<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public void setNotificaciones(ArrayList<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
    }

    public Stack<Transaccion> getPilaReversiones() {
        return pilaReversiones;
    }

    public void setPilaReversiones(Stack<Transaccion> pilaReversiones) {
        this.pilaReversiones = pilaReversiones;
    }

    public ArrayList<TransaccionProgramada> getTransaccionesProgramadas() {
        return transaccionesProgramadas;
    }

    public void setTransaccionesProgramadas(ArrayList<TransaccionProgramada> transaccionesProgramadas) {
        this.transaccionesProgramadas = transaccionesProgramadas;
    }

    @JsonProperty("saldoTotal")
    public double getSaldoTotal() {
        if (billeteras == null || billeteras.isEmpty()) return 0;

        return billeteras.values().stream()
                .mapToDouble(Billetera::getSaldo)
                .sum();
    }
}
