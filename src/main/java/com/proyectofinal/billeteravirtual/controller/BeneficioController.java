package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.model.Beneficio;
import com.proyectofinal.billeteravirtual.service.BeneficioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/beneficios")
public class BeneficioController {

    @Autowired
    private BeneficioService beneficioService;

    @PostMapping("/canjear/{cedula}/{idBilletera}/{puntos}")
    public ResponseEntity<?> canjear(
            @PathVariable String cedula,
            @PathVariable String idBilletera,
            @PathVariable int puntos) {

        Beneficio beneficio = beneficioService.canjear(
                cedula,
                idBilletera,
                puntos
        );

        if (beneficio == null) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("No se pudo realizar el canje");
        }

        return ResponseEntity.ok(beneficio);
    }

    @GetMapping("/historial/{cedula}")
    public ResponseEntity<List<Beneficio>> historial(
            @PathVariable String cedula) {

        return ResponseEntity.ok(
                beneficioService.obtenerHistorial(cedula)
        );
    }
}