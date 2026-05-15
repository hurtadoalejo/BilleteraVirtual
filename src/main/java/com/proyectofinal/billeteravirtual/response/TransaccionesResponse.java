package com.proyectofinal.billeteravirtual.response;

import com.proyectofinal.billeteravirtual.model.EstadoTransaccion;
import com.proyectofinal.billeteravirtual.model.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.Transaccion;

import java.util.Map;

public class TransaccionesResponse {

    private java.util.ArrayList<Transaccion> transacciones;

    private double dineroMovilizado;

    private Map<TipoTransaccion, Integer> frecuenciaPorTipo;

    private Map<EstadoTransaccion, Integer> cantidadPorEstado;

    public java.util.ArrayList<Transaccion> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(java.util.ArrayList<Transaccion> transacciones) {
        this.transacciones = transacciones;
    }

    public double getDineroMovilizado() {
        return dineroMovilizado;
    }

    public void setDineroMovilizado(double dineroMovilizado) {
        this.dineroMovilizado = dineroMovilizado;
    }

    public Map<TipoTransaccion, Integer> getFrecuenciaPorTipo() {
        return frecuenciaPorTipo;
    }

    public void setFrecuenciaPorTipo(Map<TipoTransaccion, Integer> frecuenciaPorTipo) {
        this.frecuenciaPorTipo = frecuenciaPorTipo;
    }

    public Map<EstadoTransaccion, Integer> getCantidadPorEstado() {
        return cantidadPorEstado;
    }

    public void setCantidadPorEstado(Map<EstadoTransaccion, Integer> cantidadPorEstado) {
        this.cantidadPorEstado = cantidadPorEstado;
    }
}
