package com.proyectofinal.billeteravirtual.model;

import java.util.List;

public class DashboardResponse {

    private int totalUsuarios;
    private int totalBilleteras;
    private int totalTransacciones;
    private double dineroMovilizado;

    private List<Transaccion> ultimasTransacciones;
    private List<Usuario> topUsuarios;

    public int getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(int totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public int getTotalBilleteras() {
        return totalBilleteras;
    }

    public void setTotalBilleteras(int totalBilleteras) {
        this.totalBilleteras = totalBilleteras;
    }

    public int getTotalTransacciones() {
        return totalTransacciones;
    }

    public void setTotalTransacciones(int totalTransacciones) {
        this.totalTransacciones = totalTransacciones;
    }

    public double getDineroMovilizado() {
        return dineroMovilizado;
    }

    public void setDineroMovilizado(double dineroMovilizado) {
        this.dineroMovilizado = dineroMovilizado;
    }

    public List<Transaccion> getUltimasTransacciones() {
        return ultimasTransacciones;
    }

    public void setUltimasTransacciones(List<Transaccion> ultimasTransacciones) {
        this.ultimasTransacciones = ultimasTransacciones;
    }

    public List<Usuario> getTopUsuarios() {
        return topUsuarios;
    }

    public void setTopUsuarios(List<Usuario> topUsuarios) {
        this.topUsuarios = topUsuarios;
    }

    // getters y setters
}