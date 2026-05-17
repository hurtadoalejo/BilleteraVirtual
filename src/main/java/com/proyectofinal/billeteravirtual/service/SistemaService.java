package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.response.DashboardResponse;
import com.proyectofinal.billeteravirtual.response.TransaccionDashboardResponse;
import org.springframework.stereotype.Service;

import java.util.*;

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
        java.util.ArrayList<Transaccion> lista = new java.util.ArrayList<>();

        for (Usuario usuario : obtenerUsuarios()) {
            for (Transaccion t : usuario.getHistorialTransacciones()) {
                if (!lista.contains(t)) {
                    lista.add(t);
                }
            }

            for (Transaccion t : usuario.getTransaccionesProgramadas()) {
                if (!lista.contains(t)) {
                    lista.add(t);
                }
            }
        }

        return lista.size();
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

    public java.util.ArrayList<TransaccionDashboardResponse> getUltimasTransacciones() {
        java.util.ArrayList<TransaccionDashboardResponse> lista = new java.util.ArrayList<>();

        for (Usuario usuario : sistema.getUsuarios().values()) {
            for (Transaccion t : usuario.getHistorialTransacciones()) {
                TransaccionDashboardResponse response = new TransaccionDashboardResponse();

                String tipo = t.getTipo().toString();
                if (t.getTipo() == TipoTransaccion.TRANSFERENCIA) {
                    Billetera origen = usuario.getBilleteras().get(t.getBilleteraOrigenId());

                    if (origen != null) {
                        tipo = "TRANSFERENCIA ENVIADA";
                    } else {
                        tipo = "TRANSFERENCIA RECIBIDA";
                    }
                }

                response.setTipo(tipo);
                response.setValor(t.getValor());
                response.setFecha(t.getFecha());

                lista.add(response);
            }
        }

        lista.sort(Comparator.comparing(TransaccionDashboardResponse::getFecha).reversed());
        java.util.ArrayList<TransaccionDashboardResponse> resultado = new java.util.ArrayList<>();

        int limite = Math.min(3, lista.size());
        for (int i = 0; i < limite; i++) {
            resultado.add(lista.get(i));
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
        response.setTopTransacciones(getTopTransacciones());
        response.setTopBilleteras(getTopBilleteras());

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

    public void actualizarGrafoBilleteras(String origenId, String destinoId) {
        sistema.getGrafoTransferenciasBilleteras().putIfAbsent(origenId, new HashMap<>());

        Map<String, Integer> conexiones = sistema.getGrafoTransferenciasBilleteras().get(origenId);

        conexiones.put(destinoId, conexiones.getOrDefault(destinoId, 0) + 1);
    }

    public void actualizarGrafoUsuarios(String cedulaOrigen, String cedulaDestino) {
        sistema.getGrafoTransferenciasUsuarios().putIfAbsent(cedulaOrigen, new HashMap<>());

        Map<String, Integer> conexiones = sistema.getGrafoTransferenciasUsuarios().get(cedulaOrigen);

        conexiones.put(cedulaDestino, conexiones.getOrDefault(cedulaDestino, 0) + 1);
    }

    public void disminuirConexionUsuarios(String cedulaOrigen, String cedulaDestino) {
        Map<String, Map<String, Integer>> grafo = sistema.getGrafoTransferenciasUsuarios();

        if (!grafo.containsKey(cedulaOrigen)) return;

        Map<String, Integer> conexiones = grafo.get(cedulaOrigen);

        if (!conexiones.containsKey(cedulaDestino)) return;

        int cantidad = conexiones.get(cedulaDestino);

        if (cantidad <= 1) {
            conexiones.remove(cedulaDestino);
            if (conexiones.isEmpty()) grafo.remove(cedulaOrigen);
        } else {
            conexiones.put(cedulaDestino, cantidad - 1);
        }
    }

    public void disminuirConexionBilleteras(String origenId, String destinoId) {
        Map<String, Map<String, Integer>> grafo = sistema.getGrafoTransferenciasBilleteras();

        if (!grafo.containsKey(origenId)) return;

        Map<String, Integer> conexiones = grafo.get(origenId);

        if (!conexiones.containsKey(destinoId)) return;

        int cantidad = conexiones.get(destinoId);

        if (cantidad <= 1) {
            conexiones.remove(destinoId);
            if (conexiones.isEmpty()) grafo.remove(origenId);;
        } else {
            conexiones.put(destinoId, cantidad - 1);
        }
    }

    public Collection<Usuario> obtenerUsuarios() {
        return sistema.getUsuarios().values();
    }

    public ArrayList<Transaccion> getTopTransacciones() {
        ArrayList<Transaccion> top = new ArrayList<>();
        int count = 0;

        for (Transaccion t : sistema.getTransaccionesPorTotal()) {
            top.add(t);
            count++;
            if (count == 3) break;
        }

        return top;
    }

    public ArrayList<Billetera> getTopBilleteras() {
        ArrayList<Billetera> top = new ArrayList<>();
        int count = 0;

        for (Billetera b : sistema.getBilleterasPorSaldo()) {
            top.add(b);
            count++;

            if (count == 3) break;
        }

        return top;
    }

    public Map<String, Map<String, Integer>> obtenerGrafoUsuarios() {
        return sistema.getGrafoTransferenciasUsuarios();
    }

    public Map<String, Map<String, Integer>> obtenerGrafoBilleteras() {
        return sistema.getGrafoTransferenciasBilleteras();
    }
}