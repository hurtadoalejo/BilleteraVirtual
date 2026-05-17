package com.proyectofinal.billeteravirtual.model;
import com.proyectofinal.billeteravirtual.enums.EstadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.NivelRiesgo;
import com.proyectofinal.billeteravirtual.enums.TipoTransaccion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Transaccion implements Comparable<Transaccion> {

    private String id;
    private LocalDateTime fecha;
    private TipoTransaccion tipo;
    private String idUsuario;
    private double valor;
    private double comision;

    private String billeteraOrigenId;
    private String billeteraDestinoId;

    private EstadoTransaccion estado;
    private int puntosGenerados;
    private NivelRiesgo nivelRiesgo = NivelRiesgo.SIN_RIESGO;
    private List<String> alertasFraude = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransaccion tipo) {
        this.tipo = tipo;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getBilleteraOrigenId() {
        return billeteraOrigenId;
    }

    public void setBilleteraOrigenId(String billeteraOrigenId) {
        this.billeteraOrigenId = billeteraOrigenId;
    }

    public String getBilleteraDestinoId() {
        return billeteraDestinoId;
    }

    public void setBilleteraDestinoId(String billeteraDestinoId) {
        this.billeteraDestinoId = billeteraDestinoId;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public void setEstado(EstadoTransaccion estado) {
        this.estado = estado;
    }

    public int getPuntosGenerados() {
        return puntosGenerados;
    }

    public void setPuntosGenerados(int puntosGenerados) {
        this.puntosGenerados = puntosGenerados;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public NivelRiesgo getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(NivelRiesgo nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public List<String> getAlertasFraude() {
        return alertasFraude;
    }

    public void setAlertasFraude(List<String> alertasFraude) {
        this.alertasFraude = alertasFraude;
    }

    @Override
    public int compareTo(Transaccion otra) {
        double totalThis = this.valor + this.comision;
        double totalOtra = otra.valor + otra.comision;

        int comparacion = Double.compare(totalOtra, totalThis);
        if (comparacion == 0) {
            return this.id.compareTo(otra.id);
        }

        return comparacion;
    }
}