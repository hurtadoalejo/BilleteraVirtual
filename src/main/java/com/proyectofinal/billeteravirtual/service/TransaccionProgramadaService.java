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
    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    public TransaccionProgramadaService(SistemaBilletera sistemaBilletera, TransaccionService transaccionService, UsuarioService usuarioService, NotificacionService notificacionService) {
        this.sistemaBilletera = sistemaBilletera;
        this.transaccionService = transaccionService;
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    public ResultadoTransaccion programarTransaccion(Usuario usuario, TipoTransaccion tipo, double valor, String billeteraOrigenId, String billeteraDestinoId, LocalDateTime fechaEjecucion) {
        Billetera origen = usuario.getBilleteras().get(billeteraOrigenId);
        if (tipo == TipoTransaccion.RETIRO) {
            if (origen.getSaldo() < valor) {
                return new ResultadoTransaccion(false, false, null, 1
                );
            }
        }

        if (tipo == TipoTransaccion.TRANSFERENCIA) {
            if (billeteraOrigenId.equals(billeteraDestinoId)) {
                return new ResultadoTransaccion(false, false, null, 3);
            }

            Billetera destino = usuarioService.buscarBilleteraGlobal(billeteraDestinoId);
            if (destino == null) {
                return new ResultadoTransaccion(false, false, null, 2);
            }

            Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(billeteraDestinoId);
            boolean mismaPersona = usuario.getCedula().equals(usuarioDestino.getCedula());

            double comision = 0;
            if (!mismaPersona) {
                double porcentaje = obtenerComision(usuario.getNivel());
                comision = valor * porcentaje;
            }

            double totalDescontar = valor + comision;
            if (origen.getSaldo() < totalDescontar) {
                return new ResultadoTransaccion(false, false, null, 1);
            }
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
        notificarProgramacion(usuario, tipo, valor, billeteraOrigenId, billeteraDestinoId, fechaEjecucion);

        return new ResultadoTransaccion(true, false, null);
    }

    private double obtenerComision(NivelUsuario nivel) {
        return switch (nivel) {
            case BRONCE -> 0.005;
            case PLATA -> 0.004;
            case ORO -> 0.003;
            case PLATINO -> 0.001;
        };
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

    private void notificarProgramacion(Usuario usuario, TipoTransaccion tipo, double valor, String billeteraOrigenId, String billeteraDestinoId, LocalDateTime fechaEjecucion) {
        switch (tipo) {
            case RECARGA -> {
                Billetera billetera = usuario.getBilleteras().get(billeteraOrigenId);
                notificacionService.enviarRecargaProgramada(usuario, billetera, valor, fechaEjecucion);
            }

            case RETIRO -> {
                Billetera billetera = usuario.getBilleteras().get(billeteraOrigenId);
                notificacionService.enviarRetiroProgramado(usuario, billetera, valor, fechaEjecucion);
            }

            case TRANSFERENCIA -> {
                Billetera origen = usuario.getBilleteras().get(billeteraOrigenId);
                Billetera destino = usuarioService.buscarBilleteraGlobal(billeteraDestinoId);

                Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(billeteraDestinoId);

                if (usuarioDestino != null && origen != null && destino != null) {
                    notificacionService.enviarTransferenciaProgramada(usuario, usuarioDestino, origen, destino, valor, fechaEjecucion);
                }
            }
        }
    }
}