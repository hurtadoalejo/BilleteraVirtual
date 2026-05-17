package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.service.FraudeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fraude")
public class FraudeController {

    private final FraudeService fraudeService;

    public FraudeController(FraudeService fraudeService) {
        this.fraudeService = fraudeService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerFraudes() {
        return ResponseEntity.ok(fraudeService.obtenerTransaccionesRiesgosas());
    }
}
