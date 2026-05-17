package com.proyectofinal.billeteravirtual.model;

import com.proyectofinal.billeteravirtual.util.Queue;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SistemaBilletera {
    private Map<String, Usuario> usuarios = new HashMap<>();
    private PriorityQueue<TransaccionProgramada> colaProgramadas = new PriorityQueue<>();
    private TreeSet<Usuario> usuariosPorPuntos = new TreeSet<>();
    private TreeSet<Transaccion> transaccionesPorTotal = new TreeSet<>();
    private TreeSet<Billetera> billeterasPorSaldo = new TreeSet<>();
    private Queue<NotificacionPendiente> notificacionesPendientes = new Queue<>();
    private Map<String, Map<String, Integer>> grafoTransferenciasBilleteras = new HashMap<>();
    private Map<String, Map<String, Integer>> grafoTransferenciasUsuarios = new HashMap<>();

    public Map<String, Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Map<String, Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public PriorityQueue<TransaccionProgramada> getColaProgramadas() {
        return colaProgramadas;
    }

    public void setColaProgramadas(PriorityQueue<TransaccionProgramada> colaProgramadas) {
        this.colaProgramadas = colaProgramadas;
    }

    public TreeSet<Usuario> getUsuariosPorPuntos() {
        return usuariosPorPuntos;
    }

    public void setUsuariosPorPuntos(TreeSet<Usuario> usuariosPorPuntos) {
        this.usuariosPorPuntos = usuariosPorPuntos;
    }

    public Queue<NotificacionPendiente> getNotificacionesPendientes() {
        return notificacionesPendientes;
    }

    public void setNotificacionesPendientes(Queue<NotificacionPendiente> notificacionesPendientes) {
        this.notificacionesPendientes = notificacionesPendientes;
    }

    public Map<String, Map<String, Integer>> getGrafoTransferenciasBilleteras() {
        return grafoTransferenciasBilleteras;
    }

    public void setGrafoTransferenciasBilleteras(Map<String, Map<String, Integer>> grafoTransferenciasBilleteras) {
        this.grafoTransferenciasBilleteras = grafoTransferenciasBilleteras;
    }

    public TreeSet<Transaccion> getTransaccionesPorTotal() {
        return transaccionesPorTotal;
    }

    public void setTransaccionesPorTotal(TreeSet<Transaccion> transaccionesPorTotal) {
        this.transaccionesPorTotal = transaccionesPorTotal;
    }

    public TreeSet<Billetera> getBilleterasPorSaldo() {
        return billeterasPorSaldo;
    }

    public void setBilleterasPorSaldo(TreeSet<Billetera> billeterasPorSaldo) {
        this.billeterasPorSaldo = billeterasPorSaldo;
    }

    public Map<String, Map<String, Integer>> getGrafoTransferenciasUsuarios() {
        return grafoTransferenciasUsuarios;
    }

    public void setGrafoTransferenciasUsuarios(Map<String, Map<String, Integer>> grafoTransferenciasUsuarios) {
        this.grafoTransferenciasUsuarios = grafoTransferenciasUsuarios;
    }
}
