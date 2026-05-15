package com.proyectofinal.billeteravirtual.response;

import java.time.LocalDateTime;

public class TransaccionDashboardResponse {

    private String tipo;
    private double valor;
    private LocalDateTime fecha;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}