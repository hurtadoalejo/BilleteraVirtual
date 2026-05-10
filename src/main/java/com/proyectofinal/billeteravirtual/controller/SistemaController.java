package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.model.DashboardResponse;
import com.proyectofinal.billeteravirtual.model.Transaccion;
import com.proyectofinal.billeteravirtual.model.Usuario;
import com.proyectofinal.billeteravirtual.service.SistemaService;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}