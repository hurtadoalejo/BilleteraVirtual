package com.proyectofinal.billeteravirtual.response;

import com.proyectofinal.billeteravirtual.model.Billetera;
import com.proyectofinal.billeteravirtual.model.Transaccion;
import com.proyectofinal.billeteravirtual.model.Usuario;
import com.proyectofinal.billeteravirtual.util.RutaTransferencia;

import java.util.List;

public class DashboardResponse {

    private int totalUsuarios;
    private int totalBilleteras;
    private int totalTransacciones;
    private double dineroMovilizado;

    private List<RutaTransferencia> topRutasUsuarios;
    private List<Usuario> topUsuarios;
    private List<Transaccion> topTransacciones;
    private List<Billetera> topBilleteras;

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

    public List<RutaTransferencia> getTopRutasUsuarios() {
        return topRutasUsuarios;
    }

    public void setTopRutasUsuarios(List<RutaTransferencia> topRutasUsuarios) {
        this.topRutasUsuarios = topRutasUsuarios;
    }

    public List<Usuario> getTopUsuarios() {
        return topUsuarios;
    }

    public void setTopUsuarios(List<Usuario> topUsuarios) {
        this.topUsuarios = topUsuarios;
    }

    public List<Transaccion> getTopTransacciones() {
        return topTransacciones;
    }

    public void setTopTransacciones(List<Transaccion> topTransacciones) {
        this.topTransacciones = topTransacciones;
    }

    public List<Billetera> getTopBilleteras() {
        return topBilleteras;
    }

    public void setTopBilleteras(List<Billetera> topBilleteras) {
        this.topBilleteras = topBilleteras;
    }
}