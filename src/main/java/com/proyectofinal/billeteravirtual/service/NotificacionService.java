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

    public void enviarTransferenciaProgramada(Usuario origen, Usuario destino, Billetera billeteraOrigen, Billetera billeteraDestino, double valor, double comision, LocalDateTime fechaEjecucion) {
        String mensaje = construirHtml(origen, TipoTransaccion.TRANSFERENCIA, valor, comision, billeteraOrigen, billeteraDestino, destino, true, fechaEjecucion);
        emailService.enviarCorreo(origen.getCorreoElectronico(), "Transferencia programada", mensaje);
    }

    public void enviarCancelacionProgramada(Usuario usuario, TransaccionProgramada transaccion) {
        String mensaje = construirHtmlCancelacion(usuario, transaccion);

        emailService.enviarCorreo(usuario.getCorreoElectronico(), "Transacción programada cancelada", mensaje);
    }

    private String construirHtmlCancelacion(Usuario usuario, TransaccionProgramada t) {
        String fecha = formatearFecha(t.getFechaEjecucion());

        String tipo = switch (t.getTipo()) {
            case RECARGA -> "La recarga";
            case RETIRO -> "El retiro";
            case TRANSFERENCIA -> "La transferencia";
        };

        String programado = switch (t.getTipo()) {
            case RECARGA, TRANSFERENCIA -> "programada";
            case RETIRO -> "programado";
        };

        return "<div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:20px;border:1px solid #e5e7eb;border-radius:10px;'>" +
                "<h2 style='color:#4d82bc;text-align:center;'>Billetera Virtual</h2>" +
                "<p>Hola <b>" + usuario.getNombreCompleto() + "</b>,</p>" +
                "<p>" + tipo +
                " " + programado +
                " para la fecha <b>" + fecha +
                "</b> fue cancelad" + (t.getTipo() == TipoTransaccion.RETIRO ? "o" : "a") +
                " correctamente.</p>" +
                "<hr style='margin:20px 0;'>" +
                "<p style='color:gray;font-size:12px;text-align:center;'>Gracias por usar Billetera Virtual</p>" +
                "</div>";
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
                    "</b> a tu billetera número <b>" + destino.getId() + "</b>.</p>";

            case RETIRO -> mensaje += "<p>Has " + (programada ? "programado" : "realizado") +
                    " un retiro de <b style='color:red;'>" + valorFormateado +
                    "</b> desde tu billetera número <b>" + origen.getId() + "</b>.</p>";

            case TRANSFERENCIA -> {
                mensaje += "<p>Has " + (programada ? "programado" : "realizado") +
                        " una transferencia de <b style='color:red;'>" + valorFormateado +
                        "</b> desde tu billetera número <b>" + infoTransferencia(origen) +
                        "</b> hacia la billetera número <b>" + infoTransferenciaDestino(destino, usuarioDestino) +
                        "</b>.</p>";

                mensaje += "<p>Comisión: <b style='color:red;'>" +
                        formatearMoneda(comision) +
                        "</b></p>";
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
                "</b> en tu billetera número <b>" + billeteraDestino.getId() + "</b>.</p>" +
                "<p><b>Fecha:</b> " + fecha + "</p>" +
                "<hr style='margin:20px 0;'><p style='color:gray;font-size:12px;text-align:center;'>Gracias por usar Billetera Virtual</p></div>";
    }

    public void enviarCancelacionTransferencia(Usuario origen, Usuario destino, Billetera billeteraOrigen, Billetera billeteraDestino, Transaccion transaccion) {
        String mensajeOrigen = construirHtmlCancelacionTransferencia(origen, destino, billeteraOrigen, billeteraDestino, transaccion, false);
        emailService.enviarCorreo(origen.getCorreoElectronico(), "Transferencia cancelada",mensajeOrigen);

        if (!origen.getCedula().equals(destino.getCedula())) {
            String mensajeDestino = construirHtmlCancelacionTransferencia(origen, destino, billeteraOrigen, billeteraDestino, transaccion, true);

            emailService.enviarCorreo(destino.getCorreoElectronico(), "Transferencia revertida", mensajeDestino);
        }
    }

    private String construirHtmlCancelacionTransferencia(Usuario origen, Usuario destino, Billetera billeteraOrigen, Billetera billeteraDestino, Transaccion transaccion, boolean recibido) {
        String fecha = formatearFecha(LocalDateTime.now());

        String mensaje =
                "<div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:20px;border:1px solid #e5e7eb;border-radius:10px;'>";

        mensaje +=
                "<h2 style='color:#4d82bc;text-align:center;'>Billetera Virtual</h2>";

        if (!recibido) {

            mensaje +=
                    "<p>Hola <b>" + origen.getNombreCompleto() + "</b>,</p>";

            mensaje +=
                    "<p>La transferencia de <b style='color:red;'>" +
                            formatearMoneda(transaccion.getValor()) +
                            "</b> realizada desde tu billetera número <b>" +
                            billeteraOrigen.getId() +
                            "</b> hacia la billetera número <b>" +
                            billeteraDestino.getId() +
                            "</b> fue cancelada correctamente.</p>";

            mensaje +=
                    "<p>La comisión de <b style='color:green;'>" +
                            formatearMoneda(transaccion.getComision()) +
                            "</b> fue devuelta a tu saldo.</p>";

        } else {

            mensaje +=
                    "<p>Hola <b>" + destino.getNombreCompleto() + "</b>,</p>";

            mensaje +=
                    "<p>La transferencia recibida de <b>" +
                            origen.getNombreCompleto() +
                            "</b> por valor de <b style='color:red;'>" +
                            formatearMoneda(transaccion.getValor()) +
                            "</b> fue revertida.</p>";

            mensaje +=
                    "<p>El dinero fue retirado de tu billetera número <b>" +
                            billeteraDestino.getId() +
                            "</b>.</p>";
        }

        mensaje +=
                "<p><b>Fecha:</b> " + fecha + "</p>";

        mensaje +=
                "<hr style='margin:20px 0;'>" +
                        "<p style='color:gray;font-size:12px;text-align:center;'>" +
                        "Gracias por usar Billetera Virtual" +
                        "</p></div>";

        return mensaje;
    }

    public void enviarFalloProgramada(Usuario usuario, TransaccionProgramada transaccion, CodigoResultadoTransaccion error) {
        String mensaje = construirHtmlFalloProgramada(usuario, transaccion, error);

        emailService.enviarCorreo(usuario.getCorreoElectronico(), "Fallo transacción programada", mensaje);
    }

    private String construirHtmlFalloProgramada(Usuario usuario, TransaccionProgramada t, CodigoResultadoTransaccion error) {

        String motivo = switch (error) {
            case SALDO_INSUFICIENTE -> "Saldo insuficiente";
            case BILLETERA_DESTINO_NO_ENCONTRADA -> "La billetera destino no existe";
            case BILLETERA_ORIGEN_NO_ENCONTRADA -> "La billetera origen no existe";
            case USUARIO_DESTINO_NO_ENCONTRADO -> "El usuario destino no existe";
            case USUARIO_NO_ENCONTRADO -> "El usuario no existe";
            case MISMA_BILLETERA -> "No puedes transferir a la misma billetera";
            case VALOR_INVALIDO -> "El valor ingresado es inválido";
            default -> "Ocurrió un error inesperado";
        };

        return "<div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:20px;border:1px solid #e5e7eb;border-radius:10px;'>" +
                "<h2 style='color:#4d82bc;text-align:center;'>Billetera Virtual</h2>" +
                "<p>Hola <b>" + usuario.getNombreCompleto() + "</b>,</p>" +
                "<p>Tu transacción programada de tipo <b>" + t.getTipo() +
                "</b> para la fecha <b>" + formatearFecha(t.getFechaEjecucion()) +
                "</b> no pudo ejecutarse.</p>" +
                "<p><b>Motivo:</b> " + motivo + "</p>" +
                "<hr style='margin:20px 0;'>" +
                "<p style='color:gray;font-size:12px;text-align:center;'>Gracias por usar Billetera Virtual</p>" +
                "</div>";
    }

    private String infoTransferencia(Billetera b) {
        return b.getId();
    }

    private String infoTransferenciaDestino(Billetera b, Usuario u) {
        return b.getId() + " - " + u.getNombreCompleto();
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