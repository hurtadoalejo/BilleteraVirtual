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

        if (usuario == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO);
        }

        if (valor <= 0) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.VALOR_INVALIDO);
        }

        Billetera origen = usuario.getBilleteras().get(billeteraOrigenId);
        if (origen == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.BILLETERA_ORIGEN_NO_ENCONTRADA);
        }

        double comision = 0;

        if (tipo == TipoTransaccion.RETIRO) {
            if (origen.getSaldo() < valor) {
                return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.SALDO_INSUFICIENTE);
            }
        }

        if (tipo == TipoTransaccion.TRANSFERENCIA) {
            if (billeteraOrigenId.equals(billeteraDestinoId)) {
                return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.MISMA_BILLETERA);
            }

            Billetera destino = usuarioService.buscarBilleteraGlobal(billeteraDestinoId);
            if (destino == null) {
                return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.BILLETERA_DESTINO_NO_ENCONTRADA);
            }

            Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(billeteraDestinoId);

            boolean mismaPersona = usuario.getCedula().equals(usuarioDestino.getCedula());

            if (!mismaPersona) {
                double porcentaje = obtenerComision(usuario.getNivel());
                comision = valor * porcentaje;
            }

            double totalDescontar = valor + comision;

            if (origen.getSaldo() < totalDescontar) {
                return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.SALDO_INSUFICIENTE);
            }
        }

        TransaccionProgramada t = new TransaccionProgramada();
        t.setId(UUID.randomUUID().toString());
        t.setUsuario(usuario);
        t.setTipo(tipo);
        t.setValor(valor);
        t.setComision(comision);
        t.setBilleteraOrigenId(billeteraOrigenId);
        t.setBilleteraDestinoId(billeteraDestinoId);
        t.setFechaEjecucion(fechaEjecucion);
        t.setEstado(EstadoTransaccion.PENDIENTE);

        sistemaBilletera.getColaProgramadas().add(t);

        usuario.getTransaccionesProgramadas().add(t);

        notificarProgramacion(usuario, tipo, valor, comision, billeteraOrigenId, billeteraDestinoId, fechaEjecucion);

        return new ResultadoTransaccion(true, false, null, CodigoResultadoTransaccion.SIN_ERROR);
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
        ResultadoTransaccion resultado = new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.ERROR_DESCONOCIDO);
        boolean exito = false;
        String cedula = t.getUsuario().getCedula();

        switch (t.getTipo()) {
            case RECARGA -> {
                resultado = transaccionService.recargar(cedula, t.getBilleteraOrigenId(), t.getValor());
                exito = resultado.isOk();
            }

            case RETIRO -> {
                resultado = transaccionService.retirar(cedula, t.getBilleteraOrigenId(), t.getValor());
                exito = resultado.isOk();
            }

            case TRANSFERENCIA -> {
                resultado = transaccionService.transferir(cedula, t.getBilleteraOrigenId(), t.getBilleteraDestinoId(), t.getValor(), t.getComision());
                exito = resultado.isOk();
            }
        }

        if (exito) {
            t.setEstado(EstadoTransaccion.COMPLETADA);
        } else {
            t.setEstado(EstadoTransaccion.FALLIDA);
            try {
                if (resultado.getCodigoError() != CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO) {
                    notificacionService.enviarFalloProgramada(t.getUsuario(), t, resultado.getCodigoError());
                }
            } catch (Exception e) {
                System.out.println("Error enviando correo de fallo: " + e.getMessage());
            }
        }
    }

    public boolean cancelarTransaccion(String idTransaccion, String cedula) {
        Usuario usuario = sistemaBilletera.getUsuarios().get(cedula);
        if (usuario == null) return false;

        for (TransaccionProgramada t : usuario.getTransaccionesProgramadas()) {
            if (t.getId().equals(idTransaccion)) {
                if (t.getEstado() != EstadoTransaccion.PENDIENTE) return false;
                t.setEstado(EstadoTransaccion.CANCELADA);

                try {
                    notificacionService.enviarCancelacionProgramada(usuario, t);
                } catch (Exception e) {
                    System.out.println("Error enviando correo: " + e.getMessage());
                }
                return true;
            }
        }

        return false;
    }

    public SistemaBilletera getSistemaBilletera() {
        return sistemaBilletera;
    }

    private void notificarProgramacion(Usuario usuario, TipoTransaccion tipo, double valor, double comision, String billeteraOrigenId, String billeteraDestinoId, LocalDateTime fechaEjecucion) {
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
                    notificacionService.enviarTransferenciaProgramada(usuario, usuarioDestino, origen, destino, valor, comision, fechaEjecucion);
                }
            }
        }
    }
}