package com.proyectofinal.billeteravirtual.model;
import java.time.LocalDateTime;

public class TransaccionProgramada extends Transaccion implements Comparable<TransaccionProgramada> {
    private Usuario usuario;
    private LocalDateTime fechaEjecucion;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaEjecucion() {
        return fechaEjecucion;
    }

    public void setFechaEjecucion(LocalDateTime fechaEjecucion) {
        this.fechaEjecucion = fechaEjecucion;
    }

    @Override
    public int compareTo(TransaccionProgramada otra) {
        return this.fechaEjecucion.compareTo(otra.fechaEjecucion);
    }
}