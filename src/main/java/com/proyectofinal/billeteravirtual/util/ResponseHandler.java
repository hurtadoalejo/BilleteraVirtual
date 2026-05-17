package com.proyectofinal.billeteravirtual.util;

import com.proyectofinal.billeteravirtual.model.ResultadoTransaccion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {

    /**
     * Maneja la respuesta de error de una transacción
     * @param resultado resultado de la transacción con su código de error
     * @param mensajeDefault mensaje por defecto en caso de error no controlado
     * @return ResponseEntity con el estado HTTP y mensaje correspondiente
     */
    public static ResponseEntity<?> manejarError(ResultadoTransaccion resultado, String mensajeDefault) {
        return switch (resultado.getCodigoError()) {

            case SALDO_INSUFICIENTE ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body("Saldo insuficiente");

            case USUARIO_NO_ENCONTRADO ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body("El usuario no existe");

            case BILLETERA_ORIGEN_NO_ENCONTRADA ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body("La billetera origen no existe");

            case BILLETERA_DESTINO_NO_ENCONTRADA ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body("La billetera destino no existe");

            case MISMA_BILLETERA ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No puedes transferir a la misma billetera");

            case VALOR_INVALIDO ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El valor ingresado es inválido");

            default ->
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mensajeDefault);
        };
    }
}