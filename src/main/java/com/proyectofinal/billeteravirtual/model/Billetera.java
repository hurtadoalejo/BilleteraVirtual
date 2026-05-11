package com.proyectofinal.billeteravirtual.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.proyectofinal.billeteravirtual.util.ArrayList;

public class Billetera {

    private String id;
    private String nombre;
    private TipoBilletera tipo;
    @JsonIgnore
    private Usuario usuario;
    private double saldo = 0;
    private EstadoBilletera estado = EstadoBilletera.ACTIVA;

    private ArrayList<Transaccion> transacciones = new ArrayList<>();

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

    public ArrayList<Transaccion> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(ArrayList<Transaccion> transacciones) {
        this.transacciones = transacciones;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Billetera)) return false;
        Billetera that = (Billetera) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}