package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.response.TransaccionesResponse;
import com.proyectofinal.billeteravirtual.util.ArrayList;
import com.proyectofinal.billeteravirtual.util.Stack;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TransaccionService {

    private final UsuarioService usuarioService;
    private final PuntosService puntosService;
    private final NotificacionService notificacionService;
    private final SistemaService sistemaService;

    public TransaccionService(UsuarioService usuarioService, PuntosService puntosService, NotificacionService notificacionService, SistemaService sistemaService) {
        this.usuarioService = usuarioService;
        this.puntosService = puntosService;
        this.notificacionService = notificacionService;
        this.sistemaService = sistemaService;
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
            usuarioDestino.getHistorialTransacciones().add(t);
        }

        NivelUsuario nivelDespues = usuarioOrigen.getNivel();
        boolean subioNivel = nivelAntes != nivelDespues;

        try {
            notificacionService.enviarTransferencia(usuarioOrigen, usuarioDestino, origen, destino, valor, comision);

        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

        sistemaService.actualizarGrafo(origen.getId(), destino.getId());
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

    public CodigoResultadoTransaccion revertirUltimaTransferencia(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO;;

        Stack<Transaccion> pila = usuario.getPilaReversiones();
        if (pila.isEmpty()) return CodigoResultadoTransaccion.TRANSACCION_NO_ENCONTRADA;;

        Transaccion t = pila.peek();
        CodigoResultadoTransaccion revertida = procesarReversion(usuario, t);

        if (revertida == CodigoResultadoTransaccion.SIN_ERROR) {
            pila.pop();
        }

        return revertida;
    }

    public CodigoResultadoTransaccion revertirTransferencia(String cedula, String idTransaccion) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO;;

        for (Transaccion t : usuario.getHistorialTransacciones()) {
            if (t.getId().equals(idTransaccion)) {
                return procesarReversion(usuario, t);
            }
        }

        return CodigoResultadoTransaccion.TRANSACCION_NO_ENCONTRADA;
    }

    private CodigoResultadoTransaccion procesarReversion(Usuario usuario, Transaccion transaccion) {
        if (transaccion == null) return CodigoResultadoTransaccion.TRANSACCION_NO_ENCONTRADA;
        if (transaccion.getTipo() != TipoTransaccion.TRANSFERENCIA) return CodigoResultadoTransaccion.ERROR_DESCONOCIDO;
        if (transaccion.getEstado() == EstadoTransaccion.REVERTIDA) return CodigoResultadoTransaccion.TRANSFERENCIA_YA_REVERTIDA;

        long segundos = java.time.Duration.between(transaccion.getFecha(), LocalDateTime.now()).getSeconds();

        if (segundos > 60) return CodigoResultadoTransaccion.REVERSA_FUERA_DE_TIEMPO;;
        Billetera origen = usuarioService.buscarBilleteraGlobal(transaccion.getBilleteraOrigenId());
        Billetera destino = usuarioService.buscarBilleteraGlobal(transaccion.getBilleteraDestinoId());

        if (origen == null) return CodigoResultadoTransaccion.BILLETERA_ORIGEN_NO_ENCONTRADA;

        if (destino == null) return CodigoResultadoTransaccion.BILLETERA_DESTINO_NO_ENCONTRADA;

        if (destino.getSaldo() < transaccion.getValor()) return CodigoResultadoTransaccion.SALDO_DESTINO_INSUFICIENTE;

        double totalDevolver = transaccion.getValor() + transaccion.getComision();
        destino.setSaldo(destino.getSaldo() - transaccion.getValor());
        origen.setSaldo(origen.getSaldo() + totalDevolver);

        puntosService.removerPuntos(usuario, transaccion.getPuntosGenerados());
        transaccion.setEstado(EstadoTransaccion.REVERTIDA);
        usuarioService.agregarHistorialReversiones(usuario.getCedula(), transaccion);

        try {
            Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(destino.getId());
            if (usuarioDestino != null) {
                notificacionService.enviarCancelacionTransferencia(usuario, usuarioDestino, origen,destino, transaccion);
            }
        } catch (Exception e) {
            System.out.println("Error enviando correo de cancelación: " + e.getMessage());
        }

        return CodigoResultadoTransaccion.SIN_ERROR;
    }

    public java.util.ArrayList<Transaccion> obtenerTodas() {
        java.util.ArrayList<Transaccion> lista = new java.util.ArrayList<>();

        for (Usuario usuario : sistemaService.obtenerUsuarios()) {
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

        return lista;
    }

    public double getMontoMovilizado(java.util.ArrayList<Transaccion> lista) {
        double total = 0;

        for (Transaccion t : lista) {
            if (t.getEstado() == EstadoTransaccion.COMPLETADA) {
                total += t.getValor();
            }
        }

        return total;
    }

    public Map<TipoTransaccion, Integer> getFrecuenciaPorTipo(java.util.ArrayList<Transaccion> lista) {
        Map<TipoTransaccion, Integer> frecuencia = new HashMap<>();

        for (Transaccion t : lista) {
            TipoTransaccion tipo = t.getTipo();
            frecuencia.put(tipo, frecuencia.getOrDefault(tipo, 0) + 1);
        }

        return frecuencia;
    }

    public Map<EstadoTransaccion, Integer> getCantidadPorEstado(java.util.ArrayList<Transaccion> lista) {
        Map<EstadoTransaccion, Integer> estados = new HashMap<>();
        for (Transaccion t : lista) {
            EstadoTransaccion estado = t.getEstado();
            estados.put(estado, estados.getOrDefault(estado, 0) + 1);
        }

        return estados;
    }

    public java.util.ArrayList<Transaccion> getHistorialOrdenado(java.util.ArrayList<Transaccion> lista) {
        lista.sort(Comparator.comparing(Transaccion::getFecha).reversed());

        return lista;
    }

    public TransaccionesResponse getTransaccionesAdmin() {

        java.util.ArrayList<Transaccion> lista = obtenerTodas();

        lista = getHistorialOrdenado(lista);

        TransaccionesResponse response = new TransaccionesResponse();

        response.setTransacciones(lista);
        response.setDineroMovilizado(getMontoMovilizado(lista));
        response.setFrecuenciaPorTipo(getFrecuenciaPorTipo(lista));
        response.setCantidadPorEstado(getCantidadPorEstado(lista));

        return response;
    }
}