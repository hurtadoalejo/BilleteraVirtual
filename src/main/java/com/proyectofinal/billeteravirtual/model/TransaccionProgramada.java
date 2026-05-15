package com.proyectofinal.billeteravirtual.model;
import java.time.LocalDateTime;

public class TransaccionProgramada extends Transaccion implements Comparable<TransaccionProgramada> {
    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public int compareTo(TransaccionProgramada otra) {
        return this.getFecha().compareTo(otra.getFecha());
    }
}