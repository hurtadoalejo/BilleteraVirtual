package com.proyectofinal.billeteravirtual.util;

public class RutaTransferencia {

    private String origen;
    private String destino;
    private int cantidad;

    public RutaTransferencia(String origen, String destino, int cantidad) {
        this.origen = origen;
        this.destino = destino;
        this.cantidad = cantidad;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public int getCantidad() {
        return cantidad;
    }
}