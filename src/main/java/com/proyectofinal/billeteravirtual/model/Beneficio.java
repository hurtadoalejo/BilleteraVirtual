package com.proyectofinal.billeteravirtual.model;

import java.time.LocalDateTime;

public class Beneficio {

    private String id;
    private int costoPuntos;
    private LocalDateTime fecha;
    private double dineroCanjeado;
    private Billetera billeteraDestino;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCostoPuntos() {
        return costoPuntos;
    }

    public void setCostoPuntos(int costoPuntos) {
        this.costoPuntos = costoPuntos;
    }

    public double getDineroCanjeado() {
        return dineroCanjeado;
    }

    public void setDineroCanjeado(double dineroCanjeado) {
        this.dineroCanjeado = dineroCanjeado;
    }

    public Billetera getBilleteraDestino() {
        return billeteraDestino;
    }

    public void setBilleteraDestino(Billetera billeteraDestino) {
        this.billeteraDestino = billeteraDestino;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
