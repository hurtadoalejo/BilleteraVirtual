package com.proyectofinal.billeteravirtual.model;

public class ResultadoTransaccion {
    private boolean ok;
    private boolean subioNivel;
    private NivelUsuario nuevoNivel;
    private int codigoError;

    public ResultadoTransaccion() {
    }

    public ResultadoTransaccion(boolean ok, boolean subioNivel, NivelUsuario nuevoNivel) {
        this.ok = ok;
        this.subioNivel = subioNivel;
        this.nuevoNivel = nuevoNivel;
    }

    public ResultadoTransaccion(boolean ok, boolean subioNivel, NivelUsuario nuevoNivel, int codigoError) {
        this.ok = ok;
        this.subioNivel = subioNivel;
        this.nuevoNivel = nuevoNivel;
        this.codigoError = codigoError;
    }

    public boolean isOk() { return ok; }
    public boolean isSubioNivel() { return subioNivel; }
    public NivelUsuario getNuevoNivel() { return nuevoNivel; }
    public int getCodigoError() {
        return codigoError;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
    public void setSubioNivel(boolean subioNivel) {
        this.subioNivel = subioNivel;
    }
    public void setNuevoNivel(NivelUsuario nuevoNivel) {
        this.nuevoNivel = nuevoNivel;
    }
    public void setCodigoError(int codigoError) {
        this.codigoError = codigoError;
    }
}