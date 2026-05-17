package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.enums.CodigoResultadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.EstadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.NivelUsuario;
import com.proyectofinal.billeteravirtual.enums.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.*;
import org.springframework.stereotype.Service;
import com.proyectofinal.billeteravirtual.response.TransaccionesResponse;
import com.proyectofinal.billeteravirtual.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.proyectofinal.billeteravirtual.util.Stack;

import java.time.Duration;
import java.util.Comparator;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransaccionService {

    private final UsuarioService usuarioService;
    private final PuntosService puntosService;
    private final NotificacionService notificacionService;
    private final BilleteraService billeteraService;
    private final SistemaService sistemaService;
    private final FraudeService fraudeService;
    private final SistemaBilletera sistema;

    public TransaccionService(UsuarioService usuarioService, PuntosService puntosService, NotificacionService notificacionService, SistemaService sistemaService, BilleteraService billeteraService, SistemaBilletera sistema, FraudeService fraudeService) {
        this.usuarioService = usuarioService;
        this.puntosService = puntosService;
        this.notificacionService = notificacionService;
        this.sistemaService = sistemaService;
        this.billeteraService = billeteraService;
        this.sistema = sistema;
        this.fraudeService = fraudeService;
    }

    /**
     * Realiza una recarga de saldo en una billetera y genera una notificación por correo electrónico.
     * @param cedula El documento de identidad del usuario.
     * @param idBilletera El identificador de la billetera destino.
     * @param valor El monto de dinero a recargar.
     * @return El objeto ResultadoTransaccion indicando el estado de la operación.
     */
    public ResultadoTransaccion recargar(String cedula, String idBilletera, double valor) {
        return recargarInterno(cedula, idBilletera, valor, true);
    }

    /**
     * Realiza una recarga de saldo en una billetera sin despachar notificación por correo.
     * @param cedula El documento de identidad del usuario.
     * @param idBilletera El identificador de la billetera destino.
     * @param valor El monto de dinero a recargar.
     * @return El objeto ResultadoTransaccion indicando el estado de la operación.
     */
    public ResultadoTransaccion recargarSinCorreo(String cedula, String idBilletera, double valor) {
        return recargarInterno(cedula, idBilletera, valor, false);
    }

    /**
     * Proceso centralizado para validar y ejecutar la recarga de saldo en una billetera específica.
     * @param cedula El documento de identidad del usuario.
     * @param idBilletera El identificador de la billetera destino.
     * @param valor El monto de dinero a recargar.
     * @param enviarCorreo Determina si se dispara o no la alerta por correo electrónico.
     * @return El objeto ResultadoTransaccion con el éxito o código de error respectivo.
     */
    private ResultadoTransaccion recargarInterno(String cedula, String idBilletera, double valor, boolean enviarCorreo) {

        Usuario usuario = buscarUsuario(cedula);
        if (usuario == null) return errorUsuario();

        Billetera billetera = buscarBilletera(usuario, idBilletera);
        if (billetera == null) return errorBilleteraOrigen();

        NivelUsuario nivelAntes = usuario.getNivel();

        billeteraService.actualizarSaldo(billetera, billetera.getSaldo() + valor);

        Transaccion t = registrarTransaccion(usuario, null, billetera, valor, 0, TipoTransaccion.RECARGA, true);

        if (enviarCorreo) enviarSeguro(() -> notificacionService.enviarRecarga(usuario, billetera, valor));

        return resultadoOk(usuario, nivelAntes, t);
    }

    /**
     * Realiza un retiro de saldo desde una billetera y despacha una notificación por correo electrónico.
     * @param cedula El documento de identidad del usuario.
     * @param idBilletera El identificador de la billetera de origen.
     * @param valor El monto de dinero a retirar.
     * @return El objeto ResultadoTransaccion indicando el estado de la operación.
     */
    public ResultadoTransaccion retirar(String cedula, String idBilletera, double valor) {
        return retirarInterno(cedula, idBilletera, valor, true);
    }

    /**
     * Realiza un retiro de saldo desde una billetera sin despachar notificación por correo.
     * @param cedula El documento de identidad del usuario.
     * @param idBilletera El identificador de la billetera de origen.
     * @param valor El monto de dinero a retirar.
     * @return El objeto ResultadoTransaccion indicando el estado de la operación.
     */
    public ResultadoTransaccion retirarSinCorreo(String cedula, String idBilletera, double valor) {
        return retirarInterno(cedula, idBilletera, valor, false);
    }

    /**
     * Proceso centralizado para validar, comprobar fondos y ejecutar el retiro de una billetera específica.
     * @param cedula El documento de identidad del usuario.
     * @param idBilletera El identificador de la billetera de origen.
     * @param valor El monto de dinero a retirar.
     * @param enviarCorreo Determina si se dispara o no la alerta por correo electrónico.
     * @return El objeto ResultadoTransaccion con el éxito o código de error por saldo insuficiente.
     */
    private ResultadoTransaccion retirarInterno(String cedula, String idBilletera, double valor, boolean enviarCorreo) {

        Usuario usuario = buscarUsuario(cedula);
        if (usuario == null) return errorUsuario();

        Billetera billetera = buscarBilletera(usuario, idBilletera);
        if (billetera == null) return errorBilleteraOrigen();

        if (billetera.getSaldo() < valor) return errorSaldo();

        NivelUsuario nivelAntes = usuario.getNivel();

        billeteraService.actualizarSaldo(billetera, billetera.getSaldo() - valor);

        Transaccion t = registrarTransaccion(usuario, billetera, null, valor, 0, TipoTransaccion.RETIRO, true);

        if (enviarCorreo) enviarSeguro(() -> notificacionService.enviarRetiro(usuario, billetera, valor));

        return resultadoOk(usuario, nivelAntes, t);
    }

    /**
     * Ejecuta una transferencia de fondos entre billeteras enviando notificaciones informativas por correo.
     * @param cedula El documento de identidad del usuario emisor.
     * @param idOrigen El identificador de la billetera origen del débito.
     * @param idDestino El identificador de la billetera global de destino.
     * @param valor El monto neto de dinero a transferir.
     * @param comisionPrevia Valor fijo opcional de comisión, si es null se calcula de manera dinámica.
     * @return El objeto ResultadoTransaccion con los detalles informativos de la transferencia.
     */
    public ResultadoTransaccion transferir(String cedula, String idOrigen, String idDestino, double valor, Double comisionPrevia) {
        return transferirInterno(cedula, idOrigen, idDestino, valor, comisionPrevia, true);
    }

    /**
     * Ejecuta una transferencia de fondos entre billeteras omitiendo el despacho de correos electrónicos.
     * @param cedula El documento de identidad del usuario emisor.
     * @param idOrigen El identificador de la billetera origen del débito.
     * @param idDestino El identificador de la billetera global de destino.
     * @param valor El monto neto de dinero a transferir.
     * @param comisionPrevia Valor fijo opcional de comisión, si es null se calcula de manera dinámica.
     * @return El objeto ResultadoTransaccion con los detalles informativos de la transferencia.
     */
    public ResultadoTransaccion transferirSinCorreo(String cedula, String idOrigen, String idDestino, double valor, Double comisionPrevia) {
        return transferirInterno(cedula, idOrigen, idDestino, valor, comisionPrevia, false);
    }

    /**
     * Proceso centralizado para estructurar de manera segura transferencias entre usuarios y mapear conexiones en el grafo.
     * @param cedula El documento de identidad del usuario emisor.
     * @param idOrigen El identificador de la billetera origen.
     * @param idDestino El identificador de la billetera destino.
     * @param valor El monto neto de dinero a transferir.
     * @param comisionPrevia Valor de comisión ya calculado, o null para procesarlo en el flujo.
     * @param enviarCorreo Determina si se disparan o no los comprobantes digitales por correo electrónico.
     * @return El objeto ResultadoTransaccion con la validación de negocio correspondiente a saldos y datos de destino.
     */
    private ResultadoTransaccion transferirInterno(String cedula, String idOrigen, String idDestino, double valor, Double comisionPrevia, boolean enviarCorreo) {

        Usuario origenUser = buscarUsuario(cedula);
        if (origenUser == null) return errorUsuario();

        if (idOrigen.equals(idDestino)) return errorMismaBilletera();

        Billetera origen = buscarBilletera(origenUser, idOrigen);
        if (origen == null) return errorBilleteraOrigen();

        Billetera destino = usuarioService.buscarBilleteraGlobal(idDestino);
        if (destino == null) return errorBilleteraDestino();

        Usuario destinoUser = usuarioService.buscarUsuarioPorBilletera(idDestino);

        if (valor <= 0) return errorValor();

        double comision = comisionPrevia != null ? comisionPrevia : calcularComision(origenUser, destinoUser, valor);
        double total = valor + comision;

        if (origen.getSaldo() < total) return errorSaldo();

        NivelUsuario nivelAntes = origenUser.getNivel();

        billeteraService.actualizarSaldo(origen, origen.getSaldo() - total);
        billeteraService.actualizarSaldo(destino, destino.getSaldo() + valor);

        Transaccion t = registrarTransaccion(origenUser, origen, destino, valor, comision, TipoTransaccion.TRANSFERENCIA, true);

        origenUser.getPilaReversiones().push(t);

        if (!origenUser.getCedula().equals(destinoUser.getCedula())) {
            destinoUser.getHistorialTransacciones().add(t);
        }

        sistemaService.actualizarGrafoBilleteras(origen.getId(), destino.getId());
        sistemaService.actualizarGrafoUsuarios(origenUser.getCedula(), destinoUser.getCedula());

        if (enviarCorreo) {
            enviarSeguro(() -> notificacionService.enviarTransferencia(origenUser, destinoUser, origen, destino, valor, comision));
        }

        return resultadoOk(origenUser, nivelAntes, t);
    }

    /**
     * Atajo interno para buscar un usuario a través del servicio de persistencia delegada.
     * @param cedula El documento de identidad.
     * @return El objeto Usuario en caso de ser encontrado.
     */
    private Usuario buscarUsuario(String cedula) {
        return usuarioService.buscarUsuarioPorCedula(cedula);
    }

    /**
     * Recupera el historial completo de transacciones asociadas a un usuario específico.
     * @param cedula El documento de identidad del usuario.
     * @return Un ArrayList con las transacciones del usuario, o null si el usuario no existe.
     */
    public ArrayList<Transaccion> obtenerHistorial(String cedula) {
        Usuario usuario = buscarUsuario(cedula);

        if (usuario == null) return null;
        return usuario.getHistorialTransacciones();
    }

    /**
     * Revierte la última transferencia exitosa del usuario utilizando el principio LIFO (Last In, First Out)
     * desde su pila de reversiones.
     * @param cedula El documento de identidad del usuario que solicita la reversión.
     * @return El código de resultado de la operación (e.g., SIN_ERROR, REVERSA_FUERA_DE_TIEMPO, etc.).
     */
    public CodigoResultadoTransaccion revertirUltimaTransferencia(String cedula) {
        Usuario usuario = buscarUsuario(cedula);
        if (usuario == null) return CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO;

        Stack<Transaccion> pila = usuario.getPilaReversiones();
        if (pila.isEmpty()) return CodigoResultadoTransaccion.TRANSACCION_NO_ENCONTRADA;

        Transaccion transaccion = pila.peek();
        CodigoResultadoTransaccion resultado = procesarReversion(usuario, transaccion);

        if (resultado == CodigoResultadoTransaccion.SIN_ERROR) pila.pop();

        return resultado;
    }

    /**
     * Busca y revierte una transferencia específica dentro del historial del usuario mediante su identificador único.
     * @param cedula El documento de identidad del usuario.
     * @param idTransaccion El identificador único de la transacción que se desea revertir.
     * @return El código de resultado que determina el éxito o la causa de falla de la reversión.
     */
    public CodigoResultadoTransaccion revertirTransferencia(String cedula, String idTransaccion) {

        Usuario usuario = buscarUsuario(cedula);

        if (usuario == null) return CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO;

        for (Transaccion transaccion : usuario.getHistorialTransacciones()) {
            if (transaccion.getId().equals(idTransaccion)) return procesarReversion(usuario, transaccion);
        }

        return CodigoResultadoTransaccion.TRANSACCION_NO_ENCONTRADA;
    }

    /**
     * Proceso centralizado de negocio que valida las reglas de tiempo (máximo 60 segundos), saldos y estados
     * para realizar la devolución de dinero, remoción de puntos y actualización de grafos por reversión.
     * @param usuario El usuario que originó la transacción.
     * @param transaccion La transacción de tipo transferencia que se va a deshacer.
     * @return CodigoResultadoTransaccion con el estatus final del proceso de reversión.
     */
    private CodigoResultadoTransaccion procesarReversion(Usuario usuario, Transaccion transaccion) {

        if (transaccion == null) {
            return CodigoResultadoTransaccion.TRANSACCION_NO_ENCONTRADA;
        }

        if (transaccion.getTipo() != TipoTransaccion.TRANSFERENCIA) {
            return CodigoResultadoTransaccion.ERROR_DESCONOCIDO;
        }

        if (transaccion.getEstado() == EstadoTransaccion.REVERTIDA) {
            return CodigoResultadoTransaccion.TRANSFERENCIA_YA_REVERTIDA;
        }

        long segundos = Duration.between(transaccion.getFecha(), LocalDateTime.now()).getSeconds();

        if (segundos > 60) {
            return CodigoResultadoTransaccion.REVERSA_FUERA_DE_TIEMPO;
        }

        Billetera origen = usuarioService.buscarBilleteraGlobal(transaccion.getBilleteraOrigenId());
        Billetera destino = usuarioService.buscarBilleteraGlobal(transaccion.getBilleteraDestinoId());

        if (origen == null) {
            return CodigoResultadoTransaccion.BILLETERA_ORIGEN_NO_ENCONTRADA;
        }

        if (destino == null) {
            return CodigoResultadoTransaccion.BILLETERA_DESTINO_NO_ENCONTRADA;
        }

        if (destino.getSaldo() < transaccion.getValor()) {
            return CodigoResultadoTransaccion.SALDO_DESTINO_INSUFICIENTE;
        }

        double totalDevolver = transaccion.getValor() + transaccion.getComision();

        billeteraService.actualizarSaldo(destino, destino.getSaldo() - transaccion.getValor());
        billeteraService.actualizarSaldo(origen, origen.getSaldo() + totalDevolver);

        puntosService.removerPuntos(usuario, transaccion.getPuntosGenerados());

        transaccion.setEstado(EstadoTransaccion.REVERTIDA);

        sistema.getTransaccionesPorTotal().remove(transaccion);

        usuarioService.agregarHistorialReversiones(usuario.getCedula(), transaccion);

        try {

            Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(destino.getId());

            if (usuarioDestino != null) {
                sistemaService.disminuirConexionUsuarios(usuario.getCedula(), usuarioDestino.getCedula());

                sistemaService.disminuirConexionBilleteras(origen.getId(), destino.getId());

                notificacionService.enviarCancelacionTransferencia(usuario, usuarioDestino, origen, destino, transaccion);
            }

        } catch (Exception e) {
            System.out.println(
                    "Error enviando correo de cancelación: " + e.getMessage()
            );
        }

        return CodigoResultadoTransaccion.SIN_ERROR;
    }

    /**
     * Calcula la sumatoria monetaria de los montos de aquellas transacciones cuyo estado actual es COMPLETADA.
     * @param lista El listado de transacciones a analizar.
     * @return El monto total de dinero efectivamente movilizado.
     */
    public double getMontoMovilizado(java.util.ArrayList<Transaccion> lista) {
        double total = 0;
        for (Transaccion transaccion : lista) {

            if (transaccion.getEstado() == EstadoTransaccion.COMPLETADA) {

                total += transaccion.getValor();
            }
        }

        return total;
    }

    /**
     * Construye un mapa de frecuencias que contabiliza cuántas transacciones corresponden a cada tipo disponible.
     * @param lista El listado de transacciones a agrupar.
     * @return Un Map que asocia cada TipoTransaccion con su respectiva cantidad de ocurrencias.
     */
    public Map<TipoTransaccion, Integer> getFrecuenciaPorTipo(java.util.ArrayList<Transaccion> lista) {
        Map<TipoTransaccion, Integer> frecuencia = new HashMap<>();

        for (Transaccion transaccion : lista) {
            TipoTransaccion tipo = transaccion.getTipo();
            Integer actual = frecuencia.get(tipo);

            if (actual == null) {
                frecuencia.put(tipo, 1);
            } else {
                frecuencia.put(tipo, actual + 1);
            }
        }

        return frecuencia;
    }

    /**
     * Construye un mapa de distribución que contabiliza la cantidad de transacciones según su estado actual.
     * @param lista El listado de transacciones a agrupar.
     * @return Un Map que vincula cada EstadoTransaccion con su cantidad de registros.
     */
    public Map<EstadoTransaccion, Integer> getCantidadPorEstado(java.util.ArrayList<Transaccion> lista) {
        Map<EstadoTransaccion, Integer> estados = new HashMap<>();

        for (Transaccion transaccion : lista) {
            EstadoTransaccion estado = transaccion.getEstado();
            Integer actual = estados.get(estado);

            if (actual == null) {
                estados.put(estado, 1);
            } else {
                estados.put(estado, actual + 1);
            }
        }

        return estados;
    }

    /**
     * Ordena de manera destructiva el listado de transacciones provisto, de la más reciente a la más antigua.
     * @param lista La lista de transacciones a ordenar.
     * @return La misma instancia de ArrayList ordenada cronológicamente en orden descendente.
     */
    public java.util.ArrayList<Transaccion> getHistorialOrdenado(java.util.ArrayList<Transaccion> lista) {

        lista.sort(Comparator.comparing(Transaccion::getFecha).reversed());
        return lista;
    }

    /**
     * Genera, unifica y empaqueta un reporte estadístico global con todas las métricas del ecosistema transaccional
     * para el módulo de administración.
     * @return Un objeto TransaccionesResponse que consolida listas ordenadas, totales y mapas de frecuencia.
     */
    public TransaccionesResponse getTransaccionesAdmin() {
        java.util.ArrayList<Transaccion> lista = sistemaService.obtenerTodasLasTransacciones();
        lista = getHistorialOrdenado(lista);

        TransaccionesResponse response = new TransaccionesResponse();

        response.setTransacciones(lista);
        response.setDineroMovilizado(getMontoMovilizado(lista));
        response.setFrecuenciaPorTipo(getFrecuenciaPorTipo(lista));
        response.setCantidadPorEstado(getCantidadPorEstado(lista));

        return response;
    }

    /**
     * Atajo interno para mapear y recuperar una billetera específica a partir de la memoria de un usuario.
     * @param usuario El objeto usuario dueño.
     * @param id El identificador único de la billetera.
     * @return El objeto Billetera correspondiente.
     */
    private Billetera buscarBilletera(Usuario usuario, String id) {
        return usuario.getBilleteras().get(id);
    }

    /**
     * Genera una respuesta estructurada con código de error indicando que el usuario no existe.
     * @return ResultadoTransaccion con estado fallido y código USUARIO_NO_ENCONTRADO.
     */
    private ResultadoTransaccion errorUsuario() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.USUARIO_NO_ENCONTRADO);
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
     * Genera una respuesta estructurada con código de error por insuficiencia de fondos monetarios.
     * @return ResultadoTransaccion con estado fallido y código SALDO_INSUFICIENTE.
     */
    private ResultadoTransaccion errorSaldo() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.SALDO_INSUFICIENTE);
    }

    /**
     * Genera una respuesta estructurada indicando que el monto ingresado para la transacción no es válido.
     * @return ResultadoTransaccion con estado fallido y código VALOR_INVALIDO.
     */
    private ResultadoTransaccion errorValor() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.VALOR_INVALIDO);
    }

    /**
     * Genera una respuesta estructurada que restringe las transferencias cuyo origen y destino coinciden sobre el mismo elemento.
     * @return ResultadoTransaccion con estado fallido y código MISMA_BILLETERA.
     */
    private ResultadoTransaccion errorMismaBilletera() {
        return new ResultadoTransaccion(false, false, null, CodigoResultadoTransaccion.MISMA_BILLETERA);
    }

    /**
     * Construye una respuesta estructurada exitosa evaluando dinámicamente si el incremento de puntos causó un ascenso de nivel.
     * @param usuario El objeto usuario que operó la transacción.
     * @param nivelAntes El nivel que ostentaba el usuario antes de procesar el flujo.
     * @param t La transacción completada.
     * @return El objeto ResultadoTransaccion con banderas de éxito y novedades de nivelación.
     */
    private ResultadoTransaccion resultadoOk(Usuario usuario, NivelUsuario nivelAntes, Transaccion t) {
        NivelUsuario nivelDespues = usuario.getNivel();
        boolean subioNivel = nivelAntes != nivelDespues;
        return new ResultadoTransaccion(true, subioNivel, nivelDespues, t);
    }

    /**
     * Ejecuta bloques de instrucciones de manera protegida capturando excepciones para evitar rupturas críticas en los flujos principales.
     * @param r El proceso ejecutable encapsulado en un Runnable.
     */
    private void enviarSeguro(Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }
    }

    /**
     * Calcula la tasa impositiva o comisión de envío según el rango de membresía del usuario emisor.
     * @param origen El usuario que envía y asume la comisión.
     * @param destino El usuario destino del dinero.
     * @param valor El importe neto de la operación.
     * @return El valor monetario de la comisión calculada, o 0 si se transfiere entre cuentas propias.
     */
    private double calcularComision(Usuario origen, Usuario destino, double valor) {
        if (origen.getCedula().equals(destino.getCedula())) return 0;

        return switch (origen.getNivel()) {
            case BRONCE -> valor * 0.005;
            case PLATA -> valor * 0.004;
            case ORO -> valor * 0.003;
            case PLATINO -> valor * 0.001;
        };
    }

    /**
     * Registra de forma completa un movimiento en el core financiero asignándole identificadores únicos y gestionando los puntos ganados.
     * @param usuario El usuario gestor de la acción.
     * @param origen La billetera de cargo.
     * @param destino La billetera de abono.
     * @param valor El monto de dinero base.
     * @param comision El recargo cobrado por el servicio.
     * @param tipo El clasificador de movimiento financiero.
     * @param generarPuntos Condicional que determina si la transacción gratifica al cliente con puntos de lealtad.
     * @return El objeto estructurado de tipo Transaccion con los estados actualizados en memoria global.
     */
    private Transaccion registrarTransaccion(Usuario usuario, Billetera origen, Billetera destino, double valor, double comision, TipoTransaccion tipo, boolean generarPuntos) {

        Transaccion t = new Transaccion();
        t.setId(UUID.randomUUID().toString());
        t.setIdUsuario(usuario.getCedula());
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
        }

        if (origen != null) origen.getTransacciones().add(t);
        if (destino != null && destino != origen) destino.getTransacciones().add(t);

        usuario.getHistorialTransacciones().add(t);
        sistema.getTransaccionesPorTotal().add(t);
        fraudeService.analizarTransaccion(usuario, t);

        return t;
    }
}