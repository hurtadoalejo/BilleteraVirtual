package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.util.ArrayList;
import com.proyectofinal.billeteravirtual.util.Stack;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransaccionService {

    private final UsuarioService usuarioService;
    private final PuntosService puntosService;
    private final NotificacionService notificacionService;

    public TransaccionService(UsuarioService usuarioService, PuntosService puntosService, NotificacionService notificacionService) {
        this.usuarioService = usuarioService;
        this.puntosService = puntosService;
        this.notificacionService = notificacionService;
    }

    public ResultadoTransaccion recargar(String cedula, String idBilletera, double valor) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO);
        }

        Billetera billetera = usuario.getBilleteras().get(idBilletera);
        if (billetera == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.BILLETERA_ORIGEN_NO_ENCONTRADA);
        }

        NivelUsuario nivelAntes = usuario.getNivel();
        billetera.setSaldo(billetera.getSaldo() + valor);
        registrarTransaccion(usuario, null, billetera, valor, 0, TipoTransaccion.RECARGA, true);
        NivelUsuario nivelDespues = usuario.getNivel();
        boolean subioNivel = nivelAntes != nivelDespues;

        try {
            notificacionService.enviarRecarga(usuario, billetera, valor);

        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

        return new ResultadoTransaccion(true, subioNivel, nivelDespues, CodigoResultadoTransaccion.SIN_ERROR);
    }

    public ResultadoTransaccion retirar(String cedula, String idBilletera, double valor) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO);
        }

        Billetera billetera = usuario.getBilleteras().get(idBilletera);
        if (billetera == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.BILLETERA_ORIGEN_NO_ENCONTRADA);
        }

        if (billetera.getSaldo() < valor) {
            return new ResultadoTransaccion(false, false, null,  CodigoResultadoTransaccion.SALDO_INSUFICIENTE);
        }

        NivelUsuario nivelAntes = usuario.getNivel();

        billetera.setSaldo(billetera.getSaldo() - valor);

        registrarTransaccion(usuario, billetera, null, valor, 0, TipoTransaccion.RETIRO, true);

        NivelUsuario nivelDespues = usuario.getNivel();
        boolean subioNivel = nivelAntes != nivelDespues;

        try {
            notificacionService.enviarRetiro(usuario, billetera, valor);

        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

        return new ResultadoTransaccion(true, subioNivel, nivelDespues, CodigoResultadoTransaccion.SIN_ERROR);
    }

    public ResultadoTransaccion transferir(String cedula, String idOrigen, String idDestino, double valor, Double comisionPrevia) {
        Usuario usuarioOrigen = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuarioOrigen == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO);
        }

        if (idOrigen.equals(idDestino)) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.MISMA_BILLETERA);
        }

        Billetera origen = usuarioOrigen.getBilleteras().get(idOrigen);
        if (origen == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.BILLETERA_ORIGEN_NO_ENCONTRADA);
        }

        Billetera destino = usuarioService.buscarBilleteraGlobal(idDestino);
        if (destino == null) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.BILLETERA_DESTINO_NO_ENCONTRADA);
        }

        Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(idDestino);

        if (valor <= 0) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.VALOR_INVALIDO);
        }

        double comision = comisionPrevia != null ? comisionPrevia: calcularComision(usuarioOrigen, usuarioDestino, valor);
        double total = valor + comision;
        if (origen.getSaldo() < total) {
            return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.SALDO_INSUFICIENTE);
        }

        NivelUsuario nivelAntes = usuarioOrigen.getNivel();

        origen.setSaldo(origen.getSaldo() - total);
        destino.setSaldo(destino.getSaldo() + valor);

        Transaccion t = registrarTransaccion(usuarioOrigen, origen, destino, valor, comision, TipoTransaccion.TRANSFERENCIA, true);

        usuarioOrigen.getPilaReversiones().push(t);

        if (!usuarioOrigen.getCedula().equals(usuarioDestino.getCedula())) {
            registrarTransaccion(usuarioDestino, origen, destino, valor, comision, TipoTransaccion.TRANSFERENCIA, false);
        }

        NivelUsuario nivelDespues = usuarioOrigen.getNivel();
        boolean subioNivel = nivelAntes != nivelDespues;

        try {
            notificacionService.enviarTransferencia(usuarioOrigen, usuarioDestino, origen, destino, valor, comision);

        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

        return new ResultadoTransaccion(true, subioNivel, nivelDespues);
    }

    private Transaccion registrarTransaccion(Usuario usuario, Billetera origen, Billetera destino, double valor, double comision, TipoTransaccion tipo, boolean generarPuntos) {
        Transaccion t = new Transaccion();
        t.setId(UUID.randomUUID().toString());
        t.setFecha(LocalDateTime.now());
        t.setTipo(tipo);
        t.setValor(valor);
        t.setComision(comision);

        t.setBilleteraOrigenId(origen != null ? origen.getId() : null);
        t.setBilleteraDestinoId(destino != null ? destino.getId() : null);
        t.setEstado(EstadoTransaccion.COMPLETADA);

        if (generarPuntos) {
            int puntos = puntosService.calcularPuntos(valor, tipo, usuario.getNivel());
            t.setPuntosGenerados(puntos);
            puntosService.aplicarPuntos(usuario, puntos);
        } else {
            t.setPuntosGenerados(0);
        }

        if (origen != null) {
            origen.getTransacciones().add(t);
        }

        if (destino != null && destino != origen) {
            destino.getTransacciones().add(t);
        }

        usuario.getHistorialTransacciones().add(t);

        return t;
    }

    private double calcularComision(Usuario origen, Usuario destino, double valor) {
        boolean mismaPersona = origen.getCedula().equals(destino.getCedula());

        if (mismaPersona) {
            return 0;
        }

        double porcentaje = switch (origen.getNivel()) {
            case BRONCE -> 0.005;
            case PLATA -> 0.004;
            case ORO -> 0.003;
            case PLATINO -> 0.001;
        };

        return valor * porcentaje;
    }

    public ArrayList<Transaccion> obtenerHistorial(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) {
            return null;
        }

        return usuario.getHistorialTransacciones();
    }

    public boolean revertirUltimaTransferencia(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return false;

        Stack<Transaccion> pila = usuario.getPilaReversiones();
        if (pila.isEmpty()) return false;

        Transaccion t = pila.peek();
        boolean revertida = procesarReversion(usuario, t);

        if (revertida) pila.pop();

        return revertida;
    }

    public boolean revertirTransferencia(String cedula, String idTransaccion) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return false;

        for (Transaccion t : usuario.getHistorialTransacciones()) {
            if (t.getId().equals(idTransaccion)) {
                return procesarReversion(usuario, t);
            }
        }

        return false;
    }

    private boolean procesarReversion(Usuario usuario, Transaccion transaccion) {
        if (transaccion == null) return false;
        if (transaccion.getTipo() != TipoTransaccion.TRANSFERENCIA) return false;
        if (transaccion.getEstado() == EstadoTransaccion.REVERTIDA) return false;

        long segundos = java.time.Duration.between(transaccion.getFecha(), LocalDateTime.now()).getSeconds();

        if (segundos > 60) return false;
        Billetera origen = usuarioService.buscarBilleteraGlobal(transaccion.getBilleteraOrigenId());
        Billetera destino = usuarioService.buscarBilleteraGlobal(transaccion.getBilleteraDestinoId());

        if (origen == null || destino == null) return false;
        if (destino.getSaldo() < transaccion.getValor()) return false;

        double totalDevolver = transaccion.getValor() + transaccion.getComision();
        destino.setSaldo(destino.getSaldo() - transaccion.getValor());
        origen.setSaldo(origen.getSaldo() + totalDevolver);

        puntosService.removerPuntos(usuario, transaccion.getPuntosGenerados());
        transaccion.setEstado(EstadoTransaccion.REVERTIDA);

        try {
            Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(destino.getId());
            if (usuarioDestino != null) {
                notificacionService.enviarCancelacionTransferencia(usuario, usuarioDestino, origen,destino, transaccion);
            }
        } catch (Exception e) {
            System.out.println("Error enviando correo de cancelación: " + e.getMessage());
        }

        return true;
    }
}