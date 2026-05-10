package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.PriorityQueue;
import java.util.UUID;

@Service
public class TransaccionProgramadaService {

    private final SistemaBilletera sistemaBilletera;
    private final TransaccionService transaccionService;

    public TransaccionProgramadaService(SistemaBilletera sistemaBilletera, TransaccionService transaccionService) {
        this.sistemaBilletera = sistemaBilletera;
        this.transaccionService = transaccionService;
    }

    public boolean programarTransaccion(Usuario usuario, TipoTransaccion tipo, double valor, String billeteraOrigenId, String billeteraDestinoId, LocalDateTime fechaEjecucion) {
        if (usuario == null) {
            return false;
        }

        if (valor <= 0) {
            return false;
        }

        if (fechaEjecucion == null) {
            return false;
        }

        if (fechaEjecucion.isBefore(LocalDateTime.now())) {
            return false;
        }

        TransaccionProgramada t = new TransaccionProgramada();

        t.setId(UUID.randomUUID().toString());
        t.setUsuario(usuario);
        t.setTipo(tipo);
        t.setValor(valor);
        t.setBilleteraOrigenId(billeteraOrigenId);
        t.setBilleteraDestinoId(billeteraDestinoId);
        t.setFechaEjecucion(fechaEjecucion);
        t.setEstado(EstadoTransaccion.PENDIENTE);

        sistemaBilletera.getColaProgramadas().add(t);
        usuario.getTransaccionesProgramadas().add(t);

        return true;
    }

    @Scheduled(fixedRate = 1000)
    public void procesarPendientes() {
        PriorityQueue<TransaccionProgramada> cola = sistemaBilletera.getColaProgramadas();

        while (!cola.isEmpty()) {
            TransaccionProgramada t = cola.peek();
            if (t.getEstado() == EstadoTransaccion.CANCELADA) {
                cola.poll();
                continue;
            }
            if (t.getFechaEjecucion().isAfter(LocalDateTime.now())) {
                break;
            }

            cola.poll();
            ejecutarTransaccion(t);
        }
    }

    private void ejecutarTransaccion(TransaccionProgramada t) {
        boolean exito = false;
        String cedula = t.getUsuario().getCedula();

        switch (t.getTipo()) {
            case RECARGA -> {
                ResultadoTransaccion resultado = transaccionService.recargar(cedula, t.getBilleteraOrigenId(), t.getValor());
                exito = resultado.isOk();
            }

            case RETIRO -> {
                ResultadoTransaccion resultado = transaccionService.retirar(cedula, t.getBilleteraOrigenId(), t.getValor());
                exito = resultado.isOk();
            }

            case TRANSFERENCIA -> {
                ResultadoTransaccion resultado = transaccionService.transferir(cedula, t.getBilleteraOrigenId(), t.getBilleteraDestinoId(), t.getValor());
                exito = resultado.isOk();
            }
        }

        if (exito) {
            t.setEstado(EstadoTransaccion.COMPLETADA);

        } else {
            t.setEstado(EstadoTransaccion.FALLIDA);
        }
    }

    public boolean cancelarTransaccion(String idTransaccion, String cedula) {
        Usuario usuario = sistemaBilletera.getUsuarios().get(cedula);
        if (usuario == null) {
            return false;
        }

        for (TransaccionProgramada t : usuario.getTransaccionesProgramadas()) {
            if (t.getId().equals(idTransaccion)) {
                if (t.getEstado() != EstadoTransaccion.PENDIENTE) {
                    return false;
                }
                t.setEstado(EstadoTransaccion.CANCELADA);
                return true;
            }
        }

        return false;
    }

    public SistemaBilletera getSistemaBilletera() {
        return sistemaBilletera;
    }
}