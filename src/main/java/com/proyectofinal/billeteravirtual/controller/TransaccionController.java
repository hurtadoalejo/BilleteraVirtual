package com.proyectofinal.billeteravirtual.controller;

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
    public ResponseEntity<?> recargar(
            @PathVariable String cedula,
            @PathVariable String idBilletera,
            @PathVariable double valor) {

        boolean ok = transaccionService.recargar(cedula, idBilletera, valor);

        if (!ok) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No se pudo realizar la recarga");
        }

        return ResponseEntity.ok("Recarga realizada correctamente");
    }

    @PostMapping("/retirar/{cedula}/{idBilletera}")
    public ResponseEntity<?> retirar(
            @PathVariable String cedula,
            @PathVariable String idBilletera,
            @RequestParam double valor) {

        boolean ok = transaccionService.retirar(cedula, idBilletera, valor);

        if (!ok) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Saldo insuficiente");
        }

        return ResponseEntity.ok("Retiro realizado correctamente");
    }

    @PostMapping("/transferir/{cedula}")
    public ResponseEntity<?> transferir(
            @PathVariable String cedula,
            @RequestParam String origen,
            @RequestParam String destino,
            @RequestParam double valor) {

        int resultado = transaccionService.transferir(cedula, origen, destino, valor);

        return switch (resultado) {
            case 1 -> ResponseEntity.status(HttpStatus.CONFLICT).body("Saldo insuficiente");
            case 2 -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("La billetera destino no existe");
            case 3 -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No puedes transferir a la misma billetera");
            case 4 -> ResponseEntity.ok("Transferencia realizada correctamente");
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error en la transferencia");
        };
    }

    @GetMapping("/historial/{cedula}")
    public ResponseEntity<?> obtenerHistorial(@PathVariable String cedula) {

        var historial = transaccionService.obtenerHistorial(cedula);

        return ResponseEntity.ok(historial);
    }
}