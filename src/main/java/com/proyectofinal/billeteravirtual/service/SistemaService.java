package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class SistemaService {

    private final SistemaBilletera sistema;

    public SistemaService(SistemaBilletera sistema) {
        this.sistema = sistema;
    }

    public int getTotalUsuarios() {
        return sistema.getUsuarios().size();
    }

    public int getTotalBilleteras() {
        int total = 0;
        for (Usuario u : sistema.getUsuarios().values()) {
            total += u.getBilleteras().size();
        }

        return total;
    }

    public int getTotalTransacciones() {
        int total = 0;
        for (Usuario u : sistema.getUsuarios().values()) {
            total += u.getHistorialTransacciones().size();
        }

        return total;
    }

    public double getDineroMovilizado() {
        double total = 0;
        for (Usuario u : sistema.getUsuarios().values()) {
            for (Transaccion t : u.getHistorialTransacciones()) {
                if (t.getEstado() == EstadoTransaccion.COMPLETADA) {
                    total += t.getValor();
                }
            }
        }

        return total;
    }

    public java.util.ArrayList<Usuario> getRankingUsuarios() {
        java.util.ArrayList<Usuario> lista = new java.util.ArrayList<>();
        for (Usuario u : sistema.getUsuariosPorPuntos()) {
            lista.add(u);
        }

        return lista;
    }

    public java.util.ArrayList<Usuario> getTopUsuarios() {
        java.util.ArrayList<Usuario> top = new java.util.ArrayList<>();
        int count = 0;

        for (Usuario u : sistema.getUsuariosPorPuntos()) {
            top.add(u);
            count++;

            if (count == 3) break;
        }

        return top;
    }

    public java.util.ArrayList<Transaccion> getUltimasTransacciones() {
        java.util.ArrayList<Transaccion> orden = new java.util.ArrayList<>();

        for (Usuario u : sistema.getUsuarios().values()) {
            for (Transaccion t : u.getHistorialTransacciones()) {
                orden.add(t);
            }
        }

        orden.sort(Comparator.comparing(Transaccion::getFecha).reversed());

        java.util.ArrayList<Transaccion> resultado = new java.util.ArrayList<>();

        int limite = Math.min(3, orden.size());
        for (int i = 0; i < limite; i++) {
            resultado.add(orden.get(i));
        }

        return resultado;
    }

    public double getPromedioTransaccionesPorUsuario() {
        int usuarios = getTotalUsuarios();
        if (usuarios == 0) return 0;

        return (double) getTotalTransacciones() / usuarios;
    }

    public double getPromedioBilleterasPorUsuario() {
        int usuarios = getTotalUsuarios();
        if (usuarios == 0) return 0;

        return (double) getTotalBilleteras() / usuarios;
    }

    public DashboardResponse getDashboard() {
        DashboardResponse response = new DashboardResponse();

        response.setTotalUsuarios(getTotalUsuarios());
        response.setTotalBilleteras(getTotalBilleteras());
        response.setTotalTransacciones(getTotalTransacciones());
        response.setDineroMovilizado(getDineroMovilizado());
        response.setUltimasTransacciones(getUltimasTransacciones());
        response.setTopUsuarios(getTopUsuarios());

        return response;
    }

    public void agregarNotificacion(NotificacionPendiente notificacion) {
        sistema.getNotificacionesPendientes().offer(notificacion);
    }

    public NotificacionPendiente obtenerSiguienteNotificacion() {
        return sistema.getNotificacionesPendientes().poll();
    }

    public boolean hayNotificacionesPendientes() {
        return !sistema.getNotificacionesPendientes().isEmpty();
    }
}