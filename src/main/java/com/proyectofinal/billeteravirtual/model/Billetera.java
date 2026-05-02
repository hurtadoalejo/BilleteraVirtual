package com.proyectofinal.billeteravirtual.model;
import java.util.List;

public class Billetera {

    private String id;
    private String nombre;
    private TipoBilletera tipo;
    private double saldo;
    private EstadoBilletera estado;

    private List<Transaccion> transacciones;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoBilletera getTipo() {
        return tipo;
    }

    public void setTipo(TipoBilletera tipo) {
        this.tipo = tipo;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public EstadoBilletera getEstado() {
        return estado;
    }

    public void setEstado(EstadoBilletera estado) {
        this.estado = estado;
    }

    public List<Transaccion> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<Transaccion> transacciones) {
        this.transacciones = transacciones;
    }
}