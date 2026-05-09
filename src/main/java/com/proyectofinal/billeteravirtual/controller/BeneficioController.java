package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.model.Beneficio;
import com.proyectofinal.billeteravirtual.service.BeneficioService;

import com.proyectofinal.billeteravirtual.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/beneficios")
public class BeneficioController {

    private final BeneficioService beneficioService;

    public BeneficioController(BeneficioService beneficioService) {
        this.beneficioService = beneficioService;
    }

    @PostMapping("/canjear/{cedula}/{idBilletera}/{puntos}")
    public ResponseEntity<?> canjear(@PathVariable String cedula, @PathVariable String idBilletera, @PathVariable int puntos) {
        Beneficio beneficio = beneficioService.canjear(cedula, idBilletera, puntos);

        if (beneficio == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("No se pudo realizar el canje");
        }

        return ResponseEntity.ok(beneficio);
    }

    @GetMapping("/historial/{cedula}")
    public ResponseEntity<List<Beneficio>> historial(@PathVariable String cedula) {
        ArrayList<Beneficio> billeteraPropias = beneficioService.obtenerHistorial(cedula);
        java.util.ArrayList<Beneficio> beneficios = new java.util.ArrayList<>();
        for (Beneficio beneficio : billeteraPropias) {
            beneficios.add(beneficio);
        }

        return ResponseEntity.ok(
                beneficios
        );
    }
}