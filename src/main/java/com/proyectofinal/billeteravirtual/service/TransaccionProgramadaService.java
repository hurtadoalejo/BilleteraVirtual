package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.enums.CodigoResultadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.EstadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.NivelUsuario;
import com.proyectofinal.billeteravirtual.enums.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.util.ResultadoTransaccion;
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

    /**
     * Programa una transacción financiera (recarga, retiro o transferencia) para ser ejecutada en una fecha futura,
     * validando los datos de negocio y encolándola en el motor de procesamiento.
     * @param usuario El usuario que solicita la programación.
     * @param tipo El tipo de transacción a ejecutar (RECARGA, RETIRO, TRANSFERENCIA).
     * @param valor El monto neto de la operación.
     * @param billeteraOrigenId El identificador de la billetera origen del dinero.
     * @param billeteraDestinoId El identificador de la billetera destino (requerido para transferencias).
     * @param fechaEjecucion La fecha y hora programada para disparar el movimiento.
     * @return El objeto ResultadoTransaccion con el estado de la reserva o su respectivo código de error.
     */
    public ResultadoTransaccion programarTransaccion(Usuario usuario, TipoTransaccion tipo, double valor, String billeteraOrigenId, String billeteraDestinoId, LocalDateTime fechaEjecucion) {

        if (usuario == null) return errorUsuario();

        if (valor <= 0) return errorValor();

        Billetera origen = obtenerBilleteraOrigen(usuario, billeteraOrigenId);

        if (origen == null) return errorBilleteraOrigen();

        double comision = 0;

        if (tipo == TipoTransaccion.RETIRO) {
            if (origen.getSaldo() < valor) {
                return errorSaldo();
            }
        }

        if (tipo == TipoTransaccion.TRANSFERENCIA) {

            if (billeteraOrigenId.equals(billeteraDestinoId)) {
                return errorMismaBilletera();
            }

            Billetera destino = usuarioService.buscarBilleteraGlobal(billeteraDestinoId);

            if (destino == null) {
                return errorBilleteraDestino();
            }

            Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(billeteraDestinoId);

            comision = calcularComision(usuario, usuarioDestino, valor);

            double total = valor + comision;

            if (origen.getSaldo() < total) {
                return errorSaldo();
            }
        }

        TransaccionProgramada t = crearTransaccionProgramada(usuario, tipo, valor, comision, billeteraOrigenId, billeteraDestinoId, fechaEjecucion);

        sistemaBilletera.getColaProgramadas().add(t);

        usuario.getTransaccionesProgramadas().add(t);

        notificarProgramacion(usuario, tipo, valor, comision, billeteraOrigenId, billeteraDestinoId, fechaEjecucion);

        return new ResultadoTransaccion(true, false, null, CodigoResultadoTransaccion.SIN_ERROR);
    }

    /**
     * Ejecuta una transacción previamente programada delegando el flujo al servicio transaccional
     * correspondiente y actualiza su estado a completada o fallida.
     * @param t La transacción programada que debe ser procesada.
     */
    private void ejecutarTransaccion(TransaccionProgramada t) {

        ResultadoTransaccion resultado = ejecutarSegunTipo(t);

        if (resultado.isOk()) {
            t.setEstado(EstadoTransaccion.COMPLETADA);
            return;
        }

        t.setEstado(EstadoTransaccion.FALLIDA);

        try {

            if (resultado.getCodigoError() != CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO) {
                notificacionService.enviarFalloProgramada(t.getUsuario(), t, resultado.getCodigoError());
            }

        } catch (Exception e) {
            System.out.println("Error enviando correo de fallo: " + e.getMessage());
        }
    }

    /**
     * Cancela una transacción programada que aún se encuentre en estado pendiente, notificando al usuario.
     * @param idTransaccion El identificador único de la transacción programada.
     * @param cedula El documento de identidad del usuario propietario de la transacción.
     * @return true si la transacción se encontró y canceló con éxito; false en caso contrario o si ya fue procesada.
     */
    public boolean cancelarTransaccion(String idTransaccion, String cedula) {

        Usuario usuario = sistemaBilletera.getUsuarios().get(cedula);

        if (usuario == null) return false;

        for (TransaccionProgramada t : usuario.getTransaccionesProgramadas()) {

            if (!t.getId().equals(idTransaccion)) {
                continue;
            }

            if (t.getEstado() != EstadoTransaccion.PENDIENTE) {
                return false;
            }

            t.setEstado(EstadoTransaccion.CANCELADA);

            try {
                notificacionService.enviarCancelacionProgramada(usuario, t);

            } catch (Exception e) {
                System.out.println("Error enviando correo: " + e.getMessage());
            }

            return true;
        }

        return false;
    }

    /**
     * Tarea programada (demonio/cron) que se ejecuta cada segundo para revisar e iniciar el procesamiento
     * de todas las transacciones pendientes cuya fecha de ejecución ya se haya cumplido.
     */
    @Scheduled(fixedRate = 1000)
    public void procesarPendientes() {

        PriorityQueue<TransaccionProgramada> cola = sistemaBilletera.getColaProgramadas();

        while (!cola.isEmpty()) {

            TransaccionProgramada t = cola.peek();

            if (t.getEstado() == EstadoTransaccion.CANCELADA) {
                cola.poll();
                continue;
            }

            if (t.getFecha().isAfter(LocalDateTime.now())) {
                break;
            }

            cola.poll();

            ejecutarTransaccion(t);
        }
    }

    /**
     * Enruta la ejecución física de la transacción programada invocando al método correspondiente de la capa de servicios.
     * @param t La transacción programada que se va a procesar en el core financiero.
     * @return El objeto ResultadoTransaccion que contiene la confirmación o rechazo del movimiento.
     */
    private ResultadoTransaccion ejecutarSegunTipo(TransaccionProgramada t) {

        String cedula = t.getUsuario().getCedula();

        return switch (t.getTipo()) {

            case RECARGA ->
                    transaccionService.recargar(cedula, t.getBilleteraOrigenId(), t.getValor());

            case RETIRO ->
                    transaccionService.retirar(cedula, t.getBilleteraOrigenId(), t.getValor());

            case TRANSFERENCIA ->
                    transaccionService.transferir(cedula, t.getBilleteraOrigenId(), t.getBilleteraDestinoId(), t.getValor(), t.getComision());
        };
    }

    /**
     * Fabrica una nueva instancia estructurada de TransaccionProgramada en estado inicial pendiente.
     * @param usuario El usuario propietario del movimiento.
     * @param tipo El tipo de operación financiera.
     * @param valor El monto de dinero base.
     * @param comision El costo calculado del servicio.
     * @param origenId El identificador de la billetera de cargo.
     * @param destinoId El identificador de la billetera de abono.
     * @param fecha La fecha y hora pactada para el cobro futuro.
     * @return El objeto TransaccionProgramada completamente inicializado.
     */
    private TransaccionProgramada crearTransaccionProgramada(Usuario usuario, TipoTransaccion tipo, double valor, double comision, String origenId, String destinoId, LocalDateTime fecha) {

        TransaccionProgramada t = new TransaccionProgramada();

        t.setId(UUID.randomUUID().toString());
        t.setUsuario(usuario);
        t.setTipo(tipo);
        t.setValor(valor);
        t.setComision(comision);
        t.setBilleteraOrigenId(origenId);
        t.setBilleteraDestinoId(destinoId);
        t.setFecha(fecha);
        t.setEstado(EstadoTransaccion.PENDIENTE);

        return t;
    }

    /**
     * Atajo interno para mapear y recuperar una billetera específica a partir de la memoria de un usuario.
     * @param usuario El objeto usuario dueño.
     * @param id El identificador único de la billetera a buscar.
     * @return El objeto Billetera correspondiente enlazado al usuario.
     */
    private Billetera obtenerBilleteraOrigen(Usuario usuario, String id) {
        return usuario.getBilleteras().get(id);
    }

    /**
     * Evalúa y calcula la comisión por concepto de transferencia entre billeteras dependiendo del nivel de lealtad.
     * @param origen El usuario emisor de los fondos.
     * @param destino El usuario receptor de los fondos.
     * @param valor El monto base sobre el cual aplicar la tasa.
     * @return El valor numérico de la comisión, o 0 si la transferencia es hacia sí mismo.
     */
    private double calcularComision(Usuario origen, Usuario destino, double valor) {

        boolean mismaPersona = origen.getCedula().equals(destino.getCedula());

        if (mismaPersona) {
            return 0;
        }

        return valor * obtenerComision(origen.getNivel());
    }

    /**
     * Genera una respuesta estructurada con código de error indicando que el usuario no existe.
     * @return ResultadoTransaccion con estado fallido y código USUARIO_NO_ENCONTRADO.
     */
    private ResultadoTransaccion errorUsuario() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO);
    }

    /**
     * Genera una respuesta estructurada indicando que el monto ingresado para la transacción no es válido.
     * @return ResultadoTransaccion con estado fallido y código VALOR_INVALIDO.
     */
    private ResultadoTransaccion errorValor() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.VALOR_INVALIDO);
    }

    /**
     * Genera una respuesta estructurada con código de error por insuficiencia de fondos monetarios.
     * @return ResultadoTransaccion con estado fallido y código SALDO_INSUFICIENTE.
     */
    private ResultadoTransaccion errorSaldo() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.SALDO_INSUFICIENTE);
    }

    /**
     * Genera una respuesta estructurada con código de error indicando que la billetera origen es inválida o no existe.
     * @return ResultadoTransaccion con estado fallido y código BILLETERA_ORIGEN_NO_ENCONTRADA.
     */
    private ResultadoTransaccion errorBilleteraOrigen() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.BILLETERA_ORIGEN_NO_ENCONTRADA);
    }

    /**
     * Genera una respuesta estructurada con código de error indicando que la billetera destino es inválida o no existe.
     * @return ResultadoTransaccion con estado fallido y código BILLETERA_DESTINO_NO_ENCONTRADA.
     */
    private ResultadoTransaccion errorBilleteraDestino() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.BILLETERA_DESTINO_NO_ENCONTRADA);
    }

    /**
     * Genera una respuesta estructurada que restringe las transferencias cuyo origen y destino coinciden sobre el mismo elemento.
     * @return ResultadoTransaccion con estado fallido y código MISMA_BILLETERA.
     */
    private ResultadoTransaccion errorMismaBilletera() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.MISMA_BILLETERA);
    }

    /**
     * Resuelve la tasa porcentual impositiva que le corresponde pagar a un usuario en base a su nivel de fidelización.
     * @param nivel El rango actual del usuario (BRONCE, PLATA, ORO, PLATINO).
     * @return El multiplicador decimal de la comisión.
     */
    private double obtenerComision(NivelUsuario nivel) {

        return switch (nivel) {
            case BRONCE -> 0.005;
            case PLATA -> 0.004;
            case ORO -> 0.003;
            case PLATINO -> 0.001;
        };
    }

    /**
     * Expone la instancia del sistema o base de datos en memoria asociada a este gestor.
     * @return La instancia activa de SistemaBilletera.
     */
    public SistemaBilletera getSistemaBilletera() {
        return sistemaBilletera;
    }

    /**
     * Despacha un correo electrónico informativo notificando al usuario que su transacción se agendó correctamente en el sistema.
     * @param usuario El usuario dueño que programó la operación.
     * @param tipo El tipo de operación agendada.
     * @param valor El valor neto del movimiento.
     * @param comision El cobro complementario calculado por el sistema.
     * @param billeteraOrigenId ID de la billetera que recibirá el débito en el futuro.
     * @param billeteraDestinoId ID de la billetera que recibirá el abono en el futuro (si aplica).
     * @param fechaEjecucion La fecha planeada para la ejecución automática.
     */
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