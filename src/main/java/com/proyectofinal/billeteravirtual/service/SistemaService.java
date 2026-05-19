package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.enums.EstadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.response.DashboardResponse;
import com.proyectofinal.billeteravirtual.response.TransaccionDashboardResponse;
import com.proyectofinal.billeteravirtual.util.NotificacionPendiente;
import com.proyectofinal.billeteravirtual.util.RutaTransferencia;
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

    /**
     * Calcula la cantidad total de billeteras creadas por todos los usuarios del sistema.
     * @return El número total de billeteras existentes.
     */
    public int getTotalBilleteras() {
        int total = 0;
        for (Usuario u : sistema.getUsuarios().values()) {
            total += u.getBilleteras().size();
        }

        return total;
    }

    /**
     * Cuenta la cantidad total de transacciones únicas (tanto del historial como programadas) en el sistema.
     * @return El número total de transacciones sin duplicados.
     */
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

    /**
     * Calcula la suma total de dinero de todas las transacciones que han sido completadas con éxito.
     * @return El monto total de dinero movilizado en el sistema.
     */
    public double getDineroMovilizado() {
        double total = 0;
        for (Usuario u : sistema.getUsuarios().values()) {
            for (Transaccion t : u.getHistorialTransacciones()) {
                if (t.getEstado() != EstadoTransaccion.COMPLETADA) {
                    continue;
                }

                if (t.getTipo() == TipoTransaccion.TRANSFERENCIA && !t.getIdUsuario().equals(u.getCedula())) {
                    continue;
                }

                total += (t.getValor() + t.getComision());
            }
        }

        return total;
    }

    /**
     * Obtiene los tres primeros usuarios con mayor cantidad de puntos en el sistema.
     * @return Un ArrayList con un máximo de 3 usuarios del tope del ranking.
     */
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

    public ArrayList<RutaTransferencia> getTopRutasUsuarios() {
        ArrayList<RutaTransferencia> rutas = obtenerRutas();
        ordenarRutas(rutas);
        return new ArrayList<>(rutas.subList(0, Math.min(3, rutas.size())));
    }

    private ArrayList<RutaTransferencia> obtenerRutas() {
        ArrayList<RutaTransferencia> rutas = new ArrayList<>();
        Map<String, Map<String, Integer>> grafo = sistema.getGrafoTransferenciasUsuarios();
        for (String origen : grafo.keySet()) {
            for (String destino : grafo.get(origen).keySet()) {
                int cantidad = grafo.get(origen).get(destino);
                rutas.add(new RutaTransferencia(origen, destino, cantidad));
            }
        }

        return rutas;
    }

    private void ordenarRutas(ArrayList<RutaTransferencia> rutas) {
        rutas.sort((a, b) -> b.getCantidad() - a.getCantidad());
    }


    /**
     * Genera y consolida un objeto con todas las métricas de negocio requeridas para la vista del Dashboard.
     * @return Un objeto DashboardResponse con estadísticas, tops y últimas actividades del sistema.
     */
    public DashboardResponse getDashboard() {
        DashboardResponse response = new DashboardResponse();

        response.setTotalUsuarios(getTotalUsuarios());
        response.setTotalBilleteras(getTotalBilleteras());
        response.setTotalTransacciones(getTotalTransacciones());
        response.setDineroMovilizado(getDineroMovilizado());
        response.setTopRutasUsuarios(getTopRutasUsuarios());
        response.setTopUsuarios(getTopUsuarios());
        response.setTopTransacciones(getTopTransacciones());
        response.setTopBilleteras(getTopBilleteras());

        return response;
    }

    /**
     * Encola una nueva notificación pendiente de envío en la estructura del sistema.
     * @param notificacion El objeto NotificacionPendiente que se desea agregar.
     */
    public void agregarNotificacion(NotificacionPendiente notificacion) {
        sistema.getNotificacionesPendientes().offer(notificacion);
    }

    /**
     * Extrae y remueve el siguiente elemento de la cola de notificaciones pendientes.
     * @return La NotificacionPendiente extraída, o null si la cola está vacía.
     */
    public NotificacionPendiente obtenerSiguienteNotificacion() {
        return sistema.getNotificacionesPendientes().poll();
    }

    /**
     * Verifica si existen mensajes o alertas que aún no han sido procesados.
     * @return true si hay notificaciones en espera; false en caso contrario.
     */
    public boolean hayNotificacionesPendientes() {
        return !sistema.getNotificacionesPendientes().isEmpty();
    }

    /**
     * Registra o incrementa el peso de una arista dirigida entre dos billeteras dentro del grafo de transferencias.
     * @param origenId El identificador de la billetera emisora.
     * @param destinoId El identificador de la billetera receptora.
     */
    public void actualizarGrafoBilleteras(String origenId, String destinoId) {
        sistema.getGrafoTransferenciasBilleteras().putIfAbsent(origenId, new HashMap<>());

        Map<String, Integer> conexiones = sistema.getGrafoTransferenciasBilleteras().get(origenId);

        conexiones.put(destinoId, conexiones.getOrDefault(destinoId, 0) + 1);
    }

    /**
     * Registra o incrementa el peso de una arista dirigida entre dos usuarios dentro del grafo de transferencias.
     * @param cedulaOrigen La cédula del usuario emisor.
     * @param cedulaDestino La cédula del usuario receptor.
     */
    public void actualizarGrafoUsuarios(String cedulaOrigen, String cedulaDestino) {
        sistema.getGrafoTransferenciasUsuarios().putIfAbsent(cedulaOrigen, new HashMap<>());

        Map<String, Integer> conexiones = sistema.getGrafoTransferenciasUsuarios().get(cedulaOrigen);

        conexiones.put(cedulaDestino, conexiones.getOrDefault(cedulaDestino, 0) + 1);
    }

    /**
     * Disminuye el peso o elimina la relación dirigida entre dos usuarios en el grafo de transferencias.
     * @param cedulaOrigen La cédula del usuario emisor.
     * @param cedulaDestino La cédula del usuario receptor.
     */
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

    /**
     * Disminuye el peso o elimina la relación dirigida entre dos billeteras en el grafo de transferencias.
     * @param origenId El identificador de la billetera emisora.
     * @param destinoId El identificador de la billetera receptora.
     */
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

    /**
     * Consolida en una sola lista todas las transacciones únicas (tanto del historial como programadas)
     * de todos los usuarios registrados en el sistema, evitando duplicados.
     * @return Un ArrayList global con el total de transacciones únicas detectadas.
     */
    public java.util.ArrayList<Transaccion> obtenerTodasLasTransacciones() {
        java.util.ArrayList<Transaccion> lista = new java.util.ArrayList<>();

        for (Usuario usuario : obtenerUsuarios()) {

            for (Transaccion transaccion : usuario.getHistorialTransacciones()) {
                if (transaccion.getTipo() == TipoTransaccion.TRANSFERENCIA && !transaccion.getIdUsuario().equals(usuario.getCedula())) {
                    continue;
                }
                lista.add(transaccion);
            }

            for (Transaccion transaccion : usuario.getTransaccionesProgramadas()) {
                if (!lista.contains(transaccion)) {
                    lista.add(transaccion);
                }
            }
        }

        return lista;
    }

    /**
     * Obtiene la colección completa de todos los usuarios almacenados en el sistema.
     * @return Una Collection con los objetos Usuario registrados.
     */
    public Collection<Usuario> obtenerUsuarios() {
        return sistema.getUsuarios().values();
    }

    /**
     * Obtiene las tres transacciones con los montos monetarios más altos registrados en el sistema.
     * @return Un ArrayList con un límite de 3 objetos Transaccion con mayor valor numérico.
     */
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

    /**
     * Obtiene las tres billeteras que poseen los saldos más altos del sistema.
     * @return Un ArrayList con un tope de 3 objetos Billetera con los mayores montos almacenados.
     */
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