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

    /**
     * Obtiene la información general del dashboard del sistema
     * @return datos consolidados del dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(sistemaService.getDashboard());
    }

    /**
     * Retorna el grafo de relaciones entre usuarios
     * @return mapa de conexiones usuario → usuario con pesos
     */
    @GetMapping("/grafo/usuarios")
    public ResponseEntity<Map<String, Map<String, Integer>>> obtenerGrafoUsuarios() {
        return ResponseEntity.ok(sistemaService.obtenerGrafoUsuarios());
    }

    /**
     * Retorna el grafo de relaciones entre billeteras
     * @return mapa de conexiones billetera → billetera con pesos
     */
    @GetMapping("/grafo/billeteras")
    public ResponseEntity<Map<String, Map<String, Integer>>> obtenerGrafoBilleteras() {
        return ResponseEntity.ok(sistemaService.obtenerGrafoBilleteras());
    }
}