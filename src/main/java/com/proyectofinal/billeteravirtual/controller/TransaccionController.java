package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.enums.CodigoResultadoTransaccion;
import com.proyectofinal.billeteravirtual.util.ResultadoTransaccion;
import com.proyectofinal.billeteravirtual.model.Transaccion;
import com.proyectofinal.billeteravirtual.service.TransaccionService;

import com.proyectofinal.billeteravirtual.util.ArrayList;
import com.proyectofinal.billeteravirtual.util.ResponseHandler;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    /**
     * Recarga saldo en una billetera
     * @param cedula cédula del usuario
     * @param idBilletera identificador de la billetera
     * @param valor monto a recargar
     * @return resultado de la transacción
     */
    @PostMapping("/recargar/{cedula}/{idBilletera}/{valor}")
    public ResponseEntity<?> recargar(@PathVariable String cedula, @PathVariable String idBilletera, @PathVariable double valor) {

        ResultadoTransaccion resultado = transaccionService.recargar(cedula, idBilletera, valor);

        if (!resultado.isOk()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No se pudo realizar la recarga");
        }

        return ResponseEntity.ok(resultado);
    }

    /**
     * Retira saldo de una billetera
     * @param cedula cédula del usuario
     * @param idBilletera identificador de la billetera
     * @param valor monto a retirar
     * @return resultado de la transacción
     */
    @PostMapping("/retirar/{cedula}/{idBilletera}")
    public ResponseEntity<?> retirar(@PathVariable String cedula, @PathVariable String idBilletera, @RequestParam double valor) {

        ResultadoTransaccion resultado = transaccionService.retirar(cedula, idBilletera, valor);

        if (!resultado.isOk()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(resultado);
        }

        return ResponseEntity.ok(resultado);
    }

    /**
     * Realiza una transferencia entre billeteras
     * @param cedula cédula del usuario
     * @param origen billetera origen
     * @param destino billetera destino
     * @param valor monto a transferir
     * @return resultado de la transferencia
     */
    @PostMapping("/transferir/{cedula}")
    public ResponseEntity<?> transferir(@PathVariable String cedula, @RequestParam String origen, @RequestParam String destino, @RequestParam double valor) {
        ResultadoTransaccion resultado = transaccionService.transferir(cedula, origen, destino, valor, null);

        if (!resultado.isOk()) {
            return ResponseHandler.manejarError(resultado, "Error en la transferencia");
        }

        return ResponseEntity.ok(resultado);
    }

    /**
     * Revertir la última transferencia del usuario
     * @param cedula cédula del usuario
     * @return resultado de la reversión
     */
    @PostMapping("/revertir/{cedula}")
    public ResponseEntity<?> revertirUltimaTransferencia(@PathVariable String cedula) {
        CodigoResultadoTransaccion resultado = transaccionService.revertirUltimaTransferencia(cedula);
        return construirRespuestaReversion(resultado);
    }

    /**
     * Revertir una transferencia específica
     * @param cedula cédula del usuario
     * @param idTransaccion identificador de la transacción
     * @return resultado de la reversión
     */
    @PostMapping("/revertir/{cedula}/{idTransaccion}")
    public ResponseEntity<?> revertirTransferencia(@PathVariable String cedula, @PathVariable String idTransaccion) {
        CodigoResultadoTransaccion resultado = transaccionService.revertirTransferencia(cedula, idTransaccion);
        return construirRespuestaReversion(resultado);
    }

    /**
     * Construye la respuesta HTTP para la reversión de transacciones
     * @param resultado código de resultado
     * @return ResponseEntity con el mensaje correspondiente
     */
    private ResponseEntity<?> construirRespuestaReversion(CodigoResultadoTransaccion resultado) {
        return switch (resultado) {
            case SIN_ERROR ->
                    ResponseEntity.ok("Transferencia revertida correctamente");

            case USUARIO_NO_ENCONTRADO ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");

            case TRANSACCION_NO_ENCONTRADA ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transacción no encontrada");

            case REVERSA_FUERA_DE_TIEMPO ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede revertir después de 60 segundos");

            case TRANSFERENCIA_YA_REVERTIDA ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body("La transferencia ya fue revertida");

            case BILLETERA_DESTINO_NO_ENCONTRADA ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body("La billetera destino ya no existe");

            case BILLETERA_ORIGEN_NO_ENCONTRADA ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body("La billetera origen ya no existe");

            case SALDO_DESTINO_INSUFICIENTE ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body("La billetera destino no tiene saldo suficiente");

            default ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo revertir la transferencia");
        };
    }

    /**
     * Obtiene el historial de transacciones de un usuario
     * @param cedula cédula del usuario
     * @return lista de transacciones
     */
    @GetMapping("/historial/{cedula}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable String cedula) {
        ArrayList<Transaccion> historial = transaccionService.obtenerHistorial(cedula);
        java.util.ArrayList<Transaccion> respuesta = new java.util.ArrayList<>();

        for (Transaccion transaccion : historial) {
            respuesta.add(transaccion);
        }

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene todas las transacciones para administración
     * @return listado de transacciones admin
     */
    @GetMapping("/admin")
    public ResponseEntity<?> dashboardTransacciones() {
        return ResponseEntity.ok(transaccionService.getTransaccionesAdmin());
    }
}