package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.model.ResultadoTransaccion;
import com.proyectofinal.billeteravirtual.service.TransaccionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    @Autowired
    private TransaccionService transaccionService;

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

    @PostMapping("/transferir/{cedula}")
    public ResponseEntity<?> transferir(@PathVariable String cedula, @RequestParam String origen, @RequestParam String destino, @RequestParam double valor) {
        ResultadoTransaccion resultado = transaccionService.transferir(cedula, origen, destino, valor);

        if (!resultado.isOk()) {
            return switch (resultado.getCodigoError()) {
                case 1 -> ResponseEntity.status(HttpStatus.CONFLICT).body("Saldo insuficiente");
                case 2 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La billetera destino no existe");
                case 3 -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No puedes transferir a la misma billetera");
                default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en la transferencia");
            };
        }

        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/revertir/{cedula}")
    public ResponseEntity<?> revertirUltimaTransferencia(@PathVariable String cedula) {
        boolean revertida = transaccionService.revertirUltimaTransferencia(cedula);

        if (!revertida) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No hay transferencias validas para cancelar");
        }

        return ResponseEntity.ok("Transferencia revertida correctamente");
    }

    @PostMapping("/revertir/{cedula}/{idTransaccion}")
    public ResponseEntity<?> revertirTransferencia(@PathVariable String cedula, @PathVariable String idTransaccion) {
        boolean revertida = transaccionService.revertirTransferencia(cedula, idTransaccion);

        if (!revertida) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede revertir una transferencia si ya pasó más de 1 minuto");
        }

        return ResponseEntity.ok("Transferencia revertida correctamente");
    }

    @GetMapping("/historial/{cedula}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable String cedula) {
        var historial = transaccionService.obtenerHistorial(cedula);

        return ResponseEntity.ok(historial);
    }
}