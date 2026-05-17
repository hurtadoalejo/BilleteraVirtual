package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.response.DashboardResponse;
import com.proyectofinal.billeteravirtual.service.SistemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sistema")
public class SistemaController {

    private final SistemaService sistemaService;

    public SistemaController(SistemaService sistemaService) {
        this.sistemaService = sistemaService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(sistemaService.getDashboard());
    }

    @GetMapping("/grafo/usuarios")
    public ResponseEntity<Map<String, Map<String, Integer>>> obtenerGrafoUsuarios() {
        return ResponseEntity.ok(sistemaService.obtenerGrafoUsuarios());
    }

    @GetMapping("/grafo/billeteras")
    public ResponseEntity<Map<String, Map<String, Integer>>> obtenerGrafoBilleteras() {
        return ResponseEntity.ok(sistemaService.obtenerGrafoBilleteras());
    }
}