package com.proyectofinal.billeteravirtual.model;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Usuario {

    private String nombreCompleto;
    private String cedula;
    private String correoElectronico;
    private String numeroTelefonico;
    private String password;

    private int puntos = 0;
    private int puntosAcumulados = 0;
    private NivelUsuario nivel = NivelUsuario.BRONCE;

    private List<Transaccion> historialTransacciones = new LinkedList<>();
    private List<Beneficio> listaBeneficios = new LinkedList<>();
    private Map<String, Billetera> billeteras = new HashMap<>();
    private List<Notificacion> notificaciones;

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getNumeroTelefonico() {
        return numeroTelefonico;
    }

    public void setNumeroTelefonico(String numeroTelefonico) {
        this.numeroTelefonico = numeroTelefonico;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public NivelUsuario getNivel() {
        return nivel;
    }

    public void setNivel(NivelUsuario nivel) {
        this.nivel = nivel;
    }

    public List<Transaccion> getHistorialTransacciones() {
        return historialTransacciones;
    }

    public void setHistorialTransacciones(List<Transaccion> historialTransacciones) {
        this.historialTransacciones = historialTransacciones;
    }

    public Map<String, Billetera> getBilleteras() {
        return billeteras;
    }

    public void setBilleteras(Map<String, Billetera> billeteras) {
        this.billeteras = billeteras;
    }

    public List<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public void setNotificaciones(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
    }

    public List<Beneficio> getListaBeneficios() {
        return listaBeneficios;
    }

    public void setListaBeneficios(List<Beneficio> listaBeneficios) {
        this.listaBeneficios = listaBeneficios;
    }

    public int getPuntosAcumulados() {
        return puntosAcumulados;
    }

    public void setPuntosAcumulados(int puntosAcumulados) {
        this.puntosAcumulados = puntosAcumulados;
    }

    @JsonProperty("saldoTotal")
    public double getSaldoTotal() {
        if (billeteras == null || billeteras.isEmpty()) return 0;

        return billeteras.values().stream()
                .mapToDouble(Billetera::getSaldo)
                .sum();
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombreCompleto='" + nombreCompleto + '\'' +
                ", cedula='" + cedula + '\'' +
                ", correoElectronico='" + correoElectronico + '\'' +
                ", numeroTelefonico='" + numeroTelefonico + '\'' +
                ", password='" + password + '\'' +
                ", puntos=" + puntos +
                ", nivel=" + nivel +
                ", billeteras=" + billeteras +
                ", historialTransacciones=" + historialTransacciones +
                ", notificaciones=" + notificaciones +
                '}';
    }
}
