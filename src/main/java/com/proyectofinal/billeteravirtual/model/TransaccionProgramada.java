package com.proyectofinal.billeteravirtual.model;
import java.time.LocalDateTime;

public class TransaccionProgramada extends Transaccion {
    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public int compareTo(Transaccion otra) {
        int comparacion = this.getFecha().compareTo(otra.getFecha());

        if (comparacion == 0) {
            return this.getId().compareTo(otra.getId());
        }

        return comparacion;
    }
}