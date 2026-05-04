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
}