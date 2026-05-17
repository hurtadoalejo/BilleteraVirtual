package com.proyectofinal.billeteravirtual.model;

import com.proyectofinal.billeteravirtual.enums.CodigoResultadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.NivelUsuario;

public class ResultadoTransaccion {
    private boolean ok;
    private boolean subioNivel;
    private NivelUsuario nuevoNivel;
    private CodigoResultadoTransaccion codigoError;
    private Transaccion transaccion;

    public ResultadoTransaccion() {
    }

    public ResultadoTransaccion(boolean ok, boolean subioNivel, NivelUsuario nuevoNivel) {
        this.ok = ok;
        this.subioNivel = subioNivel;
        this.nuevoNivel = nuevoNivel;
    }

    public ResultadoTransaccion(boolean ok, boolean subioNivel, NivelUsuario nuevoNivel, Transaccion transaccion) {
        this.ok = ok;
        this.subioNivel = subioNivel;
        this.nuevoNivel = nuevoNivel;
        this.transaccion = transaccion;
    }

    public ResultadoTransaccion(boolean ok, boolean subioNivel, NivelUsuario nuevoNivel, CodigoResultadoTransaccion codigoError) {
        this.ok = ok;
        this.subioNivel = subioNivel;
        this.nuevoNivel = nuevoNivel;
        this.codigoError = codigoError;
    }

    public ResultadoTransaccion(boolean ok, boolean subioNivel, NivelUsuario nuevoNivel, CodigoResultadoTransaccion codigoError, Transaccion transaccion) {
        this.ok = ok;
        this.subioNivel = subioNivel;
        this.nuevoNivel = nuevoNivel;
        this.codigoError = codigoError;
        this.transaccion = transaccion;
    }

    public boolean isOk() { return ok; }
    public boolean isSubioNivel() { return subioNivel; }
    public NivelUsuario getNuevoNivel() { return nuevoNivel; }
    public CodigoResultadoTransaccion getCodigoError() {
        return codigoError;
    }
    public Transaccion getTransaccion() {
        return transaccion;
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
    public void setCodigoError(CodigoResultadoTransaccion codigoError) {
        this.codigoError = codigoError;
    }
    public void setTransaccion(Transaccion transaccion) {
        this.transaccion = transaccion;
    }
}