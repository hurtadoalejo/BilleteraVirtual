package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.enums.CodigoResultadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class NotificacionService {
    private final EmailService emailService;
    private final SistemaService sistema;

    public NotificacionService(EmailService emailService, SistemaService sistema) {
        this.emailService = emailService;
        this.sistema = sistema;
    }

    /**
     * Crea y encola una notificación de correo tras realizar una recarga exitosa.
     * @param usuario El usuario que recibe la recarga.
     * @param billetera La billetera donde se depositó el dinero.
     * @param valor El monto de dinero recargado.
     */
    public void enviarRecarga(Usuario usuario, Billetera billetera, double valor) {
        String mensaje = construirHtml(usuario, TipoTransaccion.RECARGA, valor, 0, null, billetera, null, false, LocalDateTime.now());

        sistema.agregarNotificacion(new NotificacionPendiente(usuario.getCorreoElectronico(), "Recarga realizada", mensaje));
    }

    /**
     * Crea y encola una notificación para avisar que una recarga ha sido programada.
     * @param usuario El usuario que programó la recarga.
     * @param billetera La billetera destino de la recarga.
     * @param valor El monto de dinero a recargar.
     * @param fechaEjecucion La fecha y hora pactadas para ejecutar la recarga.
     */
    public void enviarRecargaProgramada(Usuario usuario, Billetera billetera, double valor, LocalDateTime fechaEjecucion) {
        String mensaje = construirHtml(usuario, TipoTransaccion.RECARGA, valor, 0, null, billetera, null, true, fechaEjecucion);

        sistema.agregarNotificacion(new NotificacionPendiente(usuario.getCorreoElectronico(), "Recarga programada", mensaje));
    }

    /**
     * Crea y encola una notificación de correo tras realizar un retiro exitoso.
     * @param usuario El usuario que realizó el retiro.
     * @param billetera La billetera desde donde se retiró el dinero.
     * @param valor El monto de dinero retirado.
     */
    public void enviarRetiro(Usuario usuario, Billetera billetera, double valor) {
        String mensaje = construirHtml(usuario, TipoTransaccion.RETIRO, valor, 0, billetera, null, null, false, LocalDateTime.now());

        sistema.agregarNotificacion(new NotificacionPendiente(usuario.getCorreoElectronico(), "Retiro realizado", mensaje));
    }

    /**
     * Crea y encola una notificación para avisar que un retiro ha sido programado.
     * @param usuario El usuario que programó el retiro.
     * @param billetera La billetera origen del retiro.
     * @param valor El monto de dinero a retirar.
     * @param fechaEjecucion La fecha y hora pactadas para ejecutar el retiro.
     */
    public void enviarRetiroProgramado(Usuario usuario, Billetera billetera, double valor, LocalDateTime fechaEjecucion) {
        String mensaje = construirHtml(usuario, TipoTransaccion.RETIRO, valor, 0, billetera, null, null, true, fechaEjecucion);

        sistema.agregarNotificacion(new NotificacionPendiente(usuario.getCorreoElectronico(), "Retiro programado", mensaje));
    }

    /**
     * Crea y encola las notificaciones de transferencia tanto para el emisor como para el receptor.
     * @param origen El usuario que envía el dinero.
     * @param destino El usuario que recibe el dinero.
     * @param billeteraOrigen La billetera desde donde sale el dinero.
     * @param billeteraDestino La billetera que almacena el dinero enviado.
     * @param valor El monto neto de la transferencia.
     * @param comision El costo operativo aplicado al emisor por la transacción.
     */
    public void enviarTransferencia(Usuario origen, Usuario destino, Billetera billeteraOrigen, Billetera billeteraDestino, double valor, double comision) {
        String mensajeOrigen = construirHtml(origen, TipoTransaccion.TRANSFERENCIA, valor, comision, billeteraOrigen, billeteraDestino, destino, false, LocalDateTime.now());

        sistema.agregarNotificacion(new NotificacionPendiente(origen.getCorreoElectronico(), "Transferencia realizada", mensajeOrigen));

        if (!origen.getCedula().equals(destino.getCedula())) {
            String mensajeDestino = construirHtmlTransferenciaRecibida(origen, destino, billeteraDestino, valor);

            sistema.agregarNotificacion(new NotificacionPendiente(destino.getCorreoElectronico(), "Transferencia recibida", mensajeDestino));
        }
    }

    /**
     * Crea y encola una notificación para el emisor indicando que una transferencia ha sido programada.
     * @param origen El usuario que programa el envío de dinero.
     * @param destino El usuario que recibirá el dinero en el futuro.
     * @param billeteraOrigen La billetera de origen de los fondos.
     * @param billeteraDestino La billetera de destino de los fondos.
     * @param valor El monto neto a transferir.
     * @param comision La comisión estimada para el movimiento.
     * @param fechaEjecucion La fecha y hora agendadas para procesar la transferencia.
     */
    public void enviarTransferenciaProgramada(Usuario origen, Usuario destino, Billetera billeteraOrigen, Billetera billeteraDestino, double valor, double comision, LocalDateTime fechaEjecucion) {
        String mensaje = construirHtml(origen, TipoTransaccion.TRANSFERENCIA, valor, comision, billeteraOrigen, billeteraDestino, destino, true, fechaEjecucion);

        sistema.agregarNotificacion(new NotificacionPendiente(origen.getCorreoElectronico(), "Transferencia programada", mensaje));
    }

    /**
     * Crea y encola una notificación informando que una transacción previamente programada fue cancelada.
     * @param usuario El usuario propietario de la transacción cancelada.
     * @param transaccion La transacción programada que fue removida.
     */
    public void enviarCancelacionProgramada(Usuario usuario, TransaccionProgramada transaccion) {
        String mensaje = construirHtmlCancelacion(usuario, transaccion);

        sistema.agregarNotificacion(new NotificacionPendiente(usuario.getCorreoElectronico(), "Transacción programada cancelada", mensaje));
    }

    /**
     * Construye la plantilla HTML del correo para la cancelación de una transacción programada.
     * @param usuario El usuario destinatario del correo.
     * @param t La transacción programada que se canceló.
     * @return Una cadena de texto con el código HTML estructurado del mensaje.
     */
    private String construirHtmlCancelacion(Usuario usuario, TransaccionProgramada t) {
        String fecha = formatearFecha(t.getFecha());

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

    /**
     * Construye dinámicamente la plantilla HTML de los correos para recargas, retiros y transferencias (inmediatas o programadas).
     * @param usuario El usuario principal involucrado.
     * @param tipo El tipo de transacción (RECARGA, RETIRO, TRANSFERENCIA).
     * @param valor El valor monetario operado.
     * @param comision El valor de la comisión cobrada (aplica a transferencias).
     * @param origen La billetera de donde salen los fondos.
     * @param destino La billetera a donde ingresan los fondos.
     * @param usuarioDestino El usuario receptor (aplica a transferencias).
     * @param programada Indica si la transacción es una operación agendada a futuro.
     * @param fechaRef La marca de tiempo correspondiente al movimiento o a su ejecución futura.
     * @return Una cadena de texto con el formato HTML del comprobante.
     */
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

    /**
     * Construye la plantilla HTML del correo de aviso para el usuario que recibe fondos por transferencia.
     * @param origen El usuario que despachó los fondos.
     * @param destino El usuario que recibe la transferencia.
     * @param billeteraDestino La billetera donde impacta el saldo positivo.
     * @param valor El monto recibido.
     * @return El texto HTML listo para ser enviado al receptor.
     */
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

    /**
     * Gestiona y encola los correos de cancelación o reversión de una transferencia efectuada.
     * @param origen El usuario emisor de la transferencia original.
     * @param destino El usuario receptor de la transferencia original.
     * @param billeteraOrigen La billetera que recuperará los fondos y comisión.
     * @param billeteraDestino La billetera de donde se debitará la reversión.
     * @param transaccion El objeto de la transferencia que se anula.
     */
    public void enviarCancelacionTransferencia(Usuario origen, Usuario destino, Billetera billeteraOrigen, Billetera billeteraDestino, Transaccion transaccion) {
        String mensajeOrigen = construirHtmlCancelacionTransferencia(origen, destino, billeteraOrigen, billeteraDestino, transaccion, false);

        sistema.agregarNotificacion(new NotificacionPendiente(origen.getCorreoElectronico(), "Transferencia cancelada", mensajeOrigen));

        if (!origen.getCedula().equals(destino.getCedula())) {
            String mensajeDestino = construirHtmlCancelacionTransferencia(origen, destino, billeteraOrigen, billeteraDestino, transaccion, true);

            sistema.agregarNotificacion(new NotificacionPendiente(destino.getCorreoElectronico(), "Transferencia revertida", mensajeDestino));
        }
    }

    /**
     * Construye la estructura HTML para notificar la reversión de una transferencia, personalizándola para el emisor o receptor.
     * @param origen El usuario emisor original.
     * @param destino El usuario receptor original.
     * @param billeteraOrigen La billetera del emisor.
     * @param billeteraDestino La billetera del receptor.
     * @param transaccion La transacción afectada.
     * @param recibido Determina el enfoque del mensaje: true para el receptor (reversión), false para el emisor (cancelación exitosa).
     * @return El diseño HTML adaptado al rol del destinatario.
     */
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

    /**
     * Crea y encola una notificación para alertar al usuario que una de sus transacciones programadas ha fallado.
     * @param usuario El propietario de la transacción fallida.
     * @param transaccion Los detalles de la transacción que no se pudo procesar.
     * @param error El código identificador del motivo del fallo.
     */
    public void enviarFalloProgramada(Usuario usuario, TransaccionProgramada transaccion, CodigoResultadoTransaccion error) {
        String mensaje = construirHtmlFalloProgramada(usuario, transaccion, error);

        sistema.agregarNotificacion(new NotificacionPendiente(usuario.getCorreoElectronico(), "Fallo transacción programada", mensaje));
    }

    /**
     * Construye la plantilla HTML que explica el motivo de fallo en la ejecución automática de una transacción programada.
     * @param usuario El usuario afectado.
     * @param t La transacción agendada que arrojó el fallo.
     * @param error El enumerador o código que detalla el inconveniente de ejecución.
     * @return El código HTML descriptivo del fallo.
     */
    private String construirHtmlFalloProgramada(Usuario usuario, TransaccionProgramada t, CodigoResultadoTransaccion error) {

        String motivo = switch (error) {
            case SALDO_INSUFICIENTE -> "Saldo insuficiente";
            case BILLETERA_DESTINO_NO_ENCONTRADA -> "La billetera destino no existe";
            case BILLETERA_ORIGEN_NO_ENCONTRADA -> "La billetera origen no existe";
            case USUARIO_NO_ENCONTRADO -> "El usuario no existe";
            case MISMA_BILLETERA -> "No puedes transferir a la misma billetera";
            case VALOR_INVALIDO -> "El valor ingresado es inválido";
            default -> "Ocurrió un error inesperado";
        };

        return "<div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;padding:20px;border:1px solid #e5e7eb;border-radius:10px;'>" +
                "<h2 style='color:#4d82bc;text-align:center;'>Billetera Virtual</h2>" +
                "<p>Hola <b>" + usuario.getNombreCompleto() + "</b>,</p>" +
                "<p>Tu transacción programada de tipo <b>" + t.getTipo() +
                "</b> para la fecha <b>" + formatearFecha(t.getFecha()) +
                "</b> no pudo ejecutarse.</p>" +
                "<p><b>Motivo:</b> " + motivo + "</p>" +
                "<hr style='margin:20px 0;'>" +
                "<p style='color:gray;font-size:12px;text-align:center;'>Gracias por usar Billetera Virtual</p>" +
                "</div>";
    }

    /**
     * Obtiene el código de identificación de una billetera dada para propósitos de información.
     * @param b La billetera de la cual obtener la información.
     * @return El identificador en cadena de la billetera.
     */
    private String infoTransferencia(Billetera b) {
        return b.getId();
    }

    /**
     * Genera una cadena compacta que combina el identificador de la billetera de destino con el nombre de su propietario.
     * @param b La billetera receptora.
     * @param u El usuario dueño de esa billetera.
     * @return Un formato compuesto de texto String (e.g. "ID - Nombre Completo").
     */
    private String infoTransferenciaDestino(Billetera b, Usuario u) {
        return b.getId() + " - " + u.getNombreCompleto();
    }

    /**
     * Formatea una fecha LocalDateTime al formato regional estándar de Colombia ("d de MMMM de yyyy, hh:mm:ss a").
     * @param fecha El objeto de fecha y hora a formatear.
     * @return La fecha formateada en formato texto limpio con indicadores a.m./p.m. normalizados.
     */
    private String formatearFecha(LocalDateTime fecha) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, hh:mm:ss a", new Locale("es", "CO"));
        return fecha.format(formatter).replace("a. m.", "a.m.").replace("p. m.", "p.m.");
    }

    /**
     * Convierte un valor de tipo numérico decimal a una cadena con formato de divisa monetaria colombiana (COP).
     * @param valor La cantidad de dinero a formatear.
     * @return El valor formateado con el símbolo de moneda local y separadores.
     */
    private String formatearMoneda(double valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return formato.format(valor);
    }

    /**
     * Tarea programada que se ejecuta periódicamente de manera continua para despachar de forma masiva los correos pendientes del sistema.
     */
    @Scheduled(fixedRate = 3000)
    public void procesarNotificaciones() {
        while (sistema.hayNotificacionesPendientes()) {
            NotificacionPendiente n = sistema.obtenerSiguienteNotificacion();
            try {
                emailService.enviarCorreo(n.getDestinatario(), n.getAsunto(), n.getMensaje());
            } catch (Exception e) {
                System.out.println("Error enviando correo: " + e.getMessage());
            }
        }
    }
}