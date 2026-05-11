package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import org.springframework.stereotype.Service;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class NotificacionService {
    private final EmailService emailService;

    public NotificacionService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void enviarRecarga(Usuario usuario, Billetera billetera, double valor) {
        String mensaje = construirHtml(usuario, TipoTransaccion.RECARGA, valor, 0, null, billetera, null, false, LocalDateTime.now());
        emailService.enviarCorreo(usuario.getCorreoElectronico(), "Recarga realizada", mensaje);
    }

    public void enviarRecargaProgramada(Usuario usuario, Billetera billetera, double valor, LocalDateTime fechaEjecucion) {
        String mensaje = construirHtml(usuario, TipoTransaccion.RECARGA, valor, 0, null, billetera, null, true, fechaEjecucion);
        emailService.enviarCorreo(usuario.getCorreoElectronico(), "Recarga programada", mensaje);
    }

    public void enviarRetiro(Usuario usuario, Billetera billetera, double valor) {
        String mensaje = construirHtml(usuario, TipoTransaccion.RETIRO, valor, 0, billetera, null, null, false, LocalDateTime.now());
        emailService.enviarCorreo(usuario.getCorreoElectronico(), "Retiro realizado", mensaje);
    }

    public void enviarRetiroProgramado(Usuario usuario, Billetera billetera, double valor, LocalDateTime fechaEjecucion) {
        String mensaje = construirHtml(usuario, TipoTransaccion.RETIRO, valor, 0, billetera, null, null, true, fechaEjecucion);
        emailService.enviarCorreo(usuario.getCorreoElectronico(), "Retiro programado", mensaje);
    }

    public void enviarTransferencia(Usuario origen, Usuario destino, Billetera billeteraOrigen, Billetera billeteraDestino, double valor, double comision) {
        String mensajeOrigen = construirHtml(origen, TipoTransaccion.TRANSFERENCIA, valor, comision, billeteraOrigen, billeteraDestino, destino, false, LocalDateTime.now());
        emailService.enviarCorreo(origen.getCorreoElectronico(), "Transferencia realizada", mensajeOrigen);

        if (!origen.getCedula().equals(destino.getCedula())) {
            String mensajeDestino = construirHtmlTransferenciaRecibida(origen, destino, billeteraDestino, valor);
            emailService.enviarCorreo(destino.getCorreoElectronico(), "Transferencia recibida", mensajeDestino);
        }
    }

    public void enviarTransferenciaProgramada(Usuario origen, Usuario destino, Billetera billeteraOrigen, Billetera billeteraDestino, double valor, LocalDateTime fechaEjecucion) {
        String mensaje = construirHtml(origen, TipoTransaccion.TRANSFERENCIA, valor, 0, billeteraOrigen, billeteraDestino, destino, true, fechaEjecucion);
        emailService.enviarCorreo(origen.getCorreoElectronico(), "Transferencia programada", mensaje);
    }

    private String construirHtml(Usuario usuario, TipoTransaccion tipo, double valor, double comision, Billetera origen, Billetera destino, Usuario usuarioDestino, boolean programada, LocalDateTime fechaRef) {
        String valorFormateado = formatearMoneda(valor);
        String fecha = formatearFecha(fechaRef);

        String mensaje = "<div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:20px;border:1px solid #e5e7eb;border-radius:10px;'>";
        mensaje += "<h2 style='color:#4d82bc;text-align:center;'>Billetera Virtual</h2>";
        mensaje += "<p>Hola <b>" + usuario.getNombreCompleto() + "</b>,</p>";

        switch (tipo) {
            case RECARGA -> mensaje += "<p>Has " + (programada ? "programado" : "realizado") +
                    " una recarga de <b style='color:green;'>" + valorFormateado +
                    "</b> a tu billetera <b>" + destino.getId() + "</b>.</p>";

            case RETIRO -> mensaje += "<p>Has " + (programada ? "programado" : "realizado") +
                    " un retiro de <b style='color:red;'>" + valorFormateado +
                    "</b> desde tu billetera <b>" + origen.getId() + "</b>.</p>";

            case TRANSFERENCIA -> {
                mensaje += "<p>Has " + (programada ? "programado" : "realizado") +
                        " una transferencia de <b style='color:red;'>" + valorFormateado +
                        "</b> desde <b>" + infoTransferencia(origen) +
                        "</b> hacia <b>" + infoTransferenciaDestino(destino, usuarioDestino) +
                        "</b>.</p>";

                mensaje += programada
                        ? "<p>Comisión: <b style='color:orange;'>Pendiente de cálculo</b></p>"
                        : "<p>Comisión: <b style='color:red;'>" + formatearMoneda(comision) + "</b></p>";
            }
        }

        mensaje += "<p><b>" + (programada ? "Fecha programada" : "Fecha") + ":</b> " + fecha + "</p>";
        mensaje += "<hr style='margin:20px 0;'><p style='color:gray;font-size:12px;text-align:center;'>Gracias por usar Billetera Virtual</p></div>";

        return mensaje;
    }

    private String construirHtmlTransferenciaRecibida(Usuario origen, Usuario destino, Billetera billeteraDestino, double valor) {
        String fecha = formatearFecha(LocalDateTime.now());

        return "<div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:20px;border:1px solid #e5e7eb;border-radius:10px;'>" +
                "<h2 style='color:#4d82bc;text-align:center;'>Billetera Virtual</h2>" +
                "<p>Hola <b>" + destino.getNombreCompleto() + "</b>,</p>" +
                "<p>Has recibido <b style='color:green;'>" + formatearMoneda(valor) +
                "</b> de <b>" + origen.getNombreCompleto() +
                "</b> en tu billetera <b>" + billeteraDestino.getId() + "</b>.</p>" +
                "<p><b>Fecha:</b> " + fecha + "</p>" +
                "<hr style='margin:20px 0;'><p style='color:gray;font-size:12px;text-align:center;'>Gracias por usar Billetera Virtual</p></div>";
    }

    private String infoTransferencia(Billetera b) {
        return b.getId();
    }

    private String infoTransferenciaDestino(Billetera b, Usuario u) {
        return u.getNombreCompleto() + " - " + b.getId();
    }

    private String formatearFecha(LocalDateTime fecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, hh:mm:ss a", new Locale("es", "CO"));
        return fecha.format(formatter).replace("a. m.", "a.m.").replace("p. m.", "p.m.");
    }

    private String formatearMoneda(double valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return formato.format(valor);
    }
}