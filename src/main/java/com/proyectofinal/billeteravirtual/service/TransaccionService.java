package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.util.ArrayList;
import com.proyectofinal.billeteravirtual.util.Stack;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransaccionService {
    private final UsuarioService usuarioService;
    private final EmailService emailService;

    public TransaccionService(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
    }

    private int calcularPuntos(double valor, TipoTransaccion tipo, NivelUsuario nivel) {

        int base = 0;

        switch (tipo) {
            case RECARGA -> base = 1;
            case RETIRO -> base = 2;
            case TRANSFERENCIA -> base = 3;
        }

        int bonus = switch (nivel) {
            case BRONCE -> 0;
            case PLATA -> 1;
            case ORO -> 2;
            case PLATINO -> 3;
        };

        return (int) (valor / 5000) * (base + bonus);
    }

    private Transaccion registrarTransaccion(Usuario usuario, Billetera origen, Billetera destino, double valor, double comision, TipoTransaccion tipo, boolean generarPuntos
    ) {
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
            int puntos = calcularPuntos(valor, tipo, usuario.getNivel());
            t.setPuntosGenerados(puntos);

            usuario.setPuntos(usuario.getPuntos() + puntos);
            usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() + puntos);
            usuarioService.actualizarRankingUsuario(usuario);
            usuarioService.actualizarNivelUsuario(usuario);
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

    public ResultadoTransaccion recargar(String cedula, String idBilletera, double valor) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return new ResultadoTransaccion(false, false, null);

        Billetera billetera = usuario.getBilleteras().get(idBilletera);
        if (billetera == null) return new ResultadoTransaccion(false, false, null);

        NivelUsuario nivelAntes = usuario.getNivel();

        billetera.setSaldo(billetera.getSaldo() + valor);

        registrarTransaccion(usuario, null, billetera, valor, 0, TipoTransaccion.RECARGA, true);

        NivelUsuario nivelDespues = usuario.getNivel();

        boolean subioNivel = nivelDespues != nivelAntes;

        try {
            enviarCorreoTransaccion(usuario, TipoTransaccion.RECARGA, valor, 0, null, billetera, null);
        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

        return new ResultadoTransaccion(true, subioNivel, nivelDespues);
    }

    public ResultadoTransaccion retirar(String cedula, String idBilletera, double valor) {

        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return new ResultadoTransaccion(false, false, null);

        Billetera billetera = usuario.getBilleteras().get(idBilletera);
        if (billetera == null) return new ResultadoTransaccion(false, false, null);

        if (billetera.getSaldo() < valor)
            return new ResultadoTransaccion(false, false, null);

        NivelUsuario nivelAntes = usuario.getNivel();

        billetera.setSaldo(billetera.getSaldo() - valor);

        registrarTransaccion(usuario, billetera, null, valor, 0, TipoTransaccion.RETIRO, true);

        NivelUsuario nivelDespues = usuario.getNivel();
        boolean subioNivel = nivelDespues != nivelAntes;

        try {
            enviarCorreoTransaccion(usuario, TipoTransaccion.RETIRO, valor, 0, billetera, null, null);
        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

        return new ResultadoTransaccion(true, subioNivel, nivelDespues);
    }

    public ResultadoTransaccion transferir(String cedula, String idOrigen, String idDestino, double valor) {

        Usuario usuarioOrigen = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuarioOrigen == null)
            return new ResultadoTransaccion(false, false, null, 2);

        if (idOrigen.equals(idDestino))
            return new ResultadoTransaccion(false, false, null, 3);

        Billetera origen = usuarioOrigen.getBilleteras().get(idOrigen);
        if (origen == null)
            return new ResultadoTransaccion(false, false, null, 2);

        Billetera destino = usuarioService.buscarBilleteraGlobal(idDestino);
        if (destino == null)
            return new ResultadoTransaccion(false, false, null, 2);

        Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(idDestino);
        if (usuarioDestino == null)
            return new ResultadoTransaccion(false, false, null, 2);

        if (valor <= 0)
            return new ResultadoTransaccion(false, false, null, 1);

        boolean mismaPersona = usuarioOrigen.getCedula().equals(usuarioDestino.getCedula());
        double comision = 0;
        if (!mismaPersona) {
            double porcentaje = obtenerComision(usuarioOrigen.getNivel());
            comision = valor * porcentaje;
        }

        double totalDescontar = valor + comision;

        if (origen.getSaldo() < totalDescontar)
            return new ResultadoTransaccion(false, false, null, 1);

        NivelUsuario nivelAntes = usuarioOrigen.getNivel();

        origen.setSaldo(origen.getSaldo() - totalDescontar);
        destino.setSaldo(destino.getSaldo() + valor);

        Transaccion t = registrarTransaccion(usuarioOrigen, origen, destino, valor, comision, TipoTransaccion.TRANSFERENCIA, true);
        usuarioOrigen.getPilaReversiones().push(t);

        if (!usuarioOrigen.getCedula().equals(usuarioDestino.getCedula())) {
            registrarTransaccion(usuarioDestino, origen, destino, valor, comision, TipoTransaccion.TRANSFERENCIA, false);
        }

        NivelUsuario nivelDespues = usuarioOrigen.getNivel();
        boolean subioNivel = nivelDespues != nivelAntes;

        try {
            enviarCorreoTransaccion(usuarioOrigen, TipoTransaccion.TRANSFERENCIA, valor, comision, origen, destino, usuarioDestino);
            enviarCorreoTransferenciaRecibida(usuarioOrigen, usuarioDestino, valor, destino);
        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

        return new ResultadoTransaccion(true, subioNivel, nivelDespues);
    }

    private boolean procesarReversion(Usuario usuario, Transaccion transaccion) {
        if (transaccion == null) {
            return false;
        }

        if (transaccion.getTipo() != TipoTransaccion.TRANSFERENCIA) {
            return false;
        }

        if (transaccion.getEstado() == EstadoTransaccion.REVERTIDA) {
            return false;
        }

        long segundos = java.time.Duration.between(transaccion.getFecha(), LocalDateTime.now()).getSeconds();
        if (segundos > 60) {
            return false;
        }

        Billetera origen = usuarioService.buscarBilleteraGlobal(transaccion.getBilleteraOrigenId());
        Billetera destino = usuarioService.buscarBilleteraGlobal(transaccion.getBilleteraDestinoId());
        if (origen == null || destino == null) {
            return false;
        }

        if (destino.getSaldo() < transaccion.getValor()) {
            return false;
        }

        double totalDevolver = transaccion.getValor() + transaccion.getComision();

        destino.setSaldo(destino.getSaldo() - transaccion.getValor());
        origen.setSaldo(origen.getSaldo() + totalDevolver);

        usuario.setPuntos(usuario.getPuntos() - transaccion.getPuntosGenerados());
        usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() - transaccion.getPuntosGenerados());
        usuarioService.actualizarRankingUsuario(usuario);
        usuarioService.actualizarNivelUsuario(usuario);

        transaccion.setEstado(EstadoTransaccion.REVERTIDA);

        return true;
    }

    public boolean revertirUltimaTransferencia(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) {
            return false;
        }

        Stack<Transaccion> pila = usuario.getPilaReversiones();
        if (pila.isEmpty()) {
            return false;
        }

        Transaccion t = pila.peek();
        boolean revertida = procesarReversion(usuario, t);

        if (revertida) {
            pila.pop();
        }

        return revertida;
    }

    public boolean revertirTransferencia(String cedula, String idTransaccion) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) {
            return false;
        }

        for (Transaccion t : usuario.getHistorialTransacciones()) {
            if (t.getId().equals(idTransaccion)) {
                return procesarReversion(usuario, t);
            }
        }

        return false;
    }

    private double obtenerComision(NivelUsuario nivel) {
        return switch (nivel) {
            case BRONCE -> 0.005;
            case PLATA -> 0.004;
            case ORO -> 0.003;
            case PLATINO -> 0.001;
        };
    }

    public ArrayList<Transaccion> obtenerHistorial(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) return null;

        return usuario.getHistorialTransacciones();
    }

    private void enviarCorreoTransaccion(Usuario usuario, TipoTransaccion tipo, double valor, double comision, Billetera origen, Billetera destino, Usuario usuarioDestino
    ) {

        String asunto = "Notificación de transacción";

        String fechaFormateada = formatearFecha(LocalDateTime.now());
        String valorFormateado = formatearMoneda(valor);

        String mensaje = "<div style='font-family:Arial, sans-serif; max-width:500px; margin:auto; padding:20px; border:1px solid #e5e7eb; border-radius:10px;'>";

        mensaje += "<h2 style='color:#4d82bc; text-align:center;'>Billetera Virtual</h2>";

        mensaje += "<p>Hola <b>" + usuario.getNombreCompleto() + "</b>,</p>";

        switch (tipo) {
            case RECARGA ->
                    mensaje += "<p>Has recargado <b style='color:green;'>" + valorFormateado +
                            "</b> a tu billetera <b>" + destino.getId() + "</b>.</p>";

            case RETIRO ->
                    mensaje += "<p>Has retirado <b style='color:red;'>" + valorFormateado +
                            "</b> desde tu billetera <b>" + origen.getId() + "</b>.</p>";

            case TRANSFERENCIA ->
                    mensaje += "<p>Has transferido <b style='color:red;'>" + valorFormateado +
                            "</b> desde la billetera <b>" + origen.getId() +
                            "</b> a <b>" + usuarioDestino.getNombreCompleto() + "</b>.</p>" +
                            "<p>Comisión: <b style='color:red;'>" + formatearMoneda(comision) + "</b></p>";
        }

        mensaje += "<p><b>Fecha:</b> " + fechaFormateada + "</p>";

        mensaje += "<hr style='margin:20px 0;'>";

        mensaje += "<p style='color:gray; font-size:12px; text-align:center;'>Gracias por usar Billetera Virtual</p>";

        mensaje += "</div>";

        emailService.enviarCorreo(usuario.getCorreoElectronico(), asunto, mensaje);
    }

    private void enviarCorreoTransferenciaRecibida(Usuario origen, Usuario destino, double valor, Billetera billeteraDestino) {

        if (origen.getCedula().equals(destino.getCedula())) return;

        String asunto = "Transferencia recibida";

        String fechaFormateada = formatearFecha(LocalDateTime.now());
        String valorFormateado = formatearMoneda(valor);

        String mensaje = "<div style='font-family:Arial, sans-serif; max-width:500px; margin:auto; padding:20px; border:1px solid #e5e7eb; border-radius:10px;'>";

        mensaje += "<h2 style='color:#4d82bc; text-align:center;'>Billetera Virtual</h2>";

        mensaje += "<p>Hola <b>" + destino.getNombreCompleto() + "</b>,</p>";

        mensaje += "<p>Has recibido <b style='color:green;'>" + valorFormateado +
                "</b> de <b>" + origen.getNombreCompleto() +
                "</b> en tu billetera <b>" + billeteraDestino.getId() + "</b>.</p>";

        mensaje += "<p><b>Fecha:</b> " + fechaFormateada + "</p>";

        mensaje += "<hr style='margin:20px 0;'>";

        mensaje += "<p style='color:gray; font-size:12px; text-align:center;'>Gracias por usar Billetera Virtual</p>";

        mensaje += "</div>";

        emailService.enviarCorreo(destino.getCorreoElectronico(), asunto, mensaje);
    }

    private String formatearFecha(LocalDateTime fecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, hh:mm:ss a", new Locale("es", "CO"));
        return fecha.format(formatter)
                .replace("a. m.", "a.m.")
                .replace("p. m.", "p.m.");
    }

    private String formatearMoneda(double valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return formato.format(valor);
    }
}
