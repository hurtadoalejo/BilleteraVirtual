package com.proyectofinal.billeteravirtual.response;

import com.proyectofinal.billeteravirtual.model.EstadoBilletera;
import com.proyectofinal.billeteravirtual.model.TipoBilletera;

public class BilleteraResponse {
    private String id;
    private String nombre;
    private TipoBilletera tipo;
    private EstadoBilletera estado;
    private double saldo;

    private String usuarioNombre;
    private String usuarioId;
    private int movimientos;

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

    public EstadoBilletera getEstado() {
        return estado;
    }

    public void setEstado(EstadoBilletera estado) {
        this.estado = estado;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(int movimientos) {
        this.movimientos = movimientos;
    }
}
