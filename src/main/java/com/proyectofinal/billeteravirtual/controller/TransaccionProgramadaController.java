package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.model.TransaccionProgramada;
import com.proyectofinal.billeteravirtual.model.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.Usuario;
import com.proyectofinal.billeteravirtual.service.TransaccionProgramadaService;
import com.proyectofinal.billeteravirtual.service.UsuarioService;

import com.proyectofinal.billeteravirtual.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

@RestController
@RequestMapping("/transacciones-programadas")
public class TransaccionProgramadaController {

    private final TransaccionProgramadaService transaccionProgramadaService;
    private final UsuarioService usuarioService;

    public TransaccionProgramadaController(TransaccionProgramadaService transaccionProgramadaService, UsuarioService usuarioService) {
        this.transaccionProgramadaService = transaccionProgramadaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/programar")
    public ResponseEntity<?> programar(@RequestParam String cedula, @RequestParam TipoTransaccion tipo, @RequestParam double valor, @RequestParam(required = false) String origen, @RequestParam(required = false) String destino, @RequestParam String fecha) {

        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        LocalDateTime fechaEjecucion = LocalDateTime.parse(fecha);
        boolean creado = transaccionProgramadaService.programarTransaccion(usuario, tipo, valor, origen, destino, fechaEjecucion);

        if (!creado) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("La transaccion se debe programar mínimo un minuto después de la hora actual.");
        }

        return ResponseEntity.ok("Transacción programada correctamente");
    }

    @PostMapping("/cancelar/{cedula}/{id}")
    public ResponseEntity<?> cancelar(@PathVariable String cedula, @PathVariable String id) {
        boolean cancelada = transaccionProgramadaService.cancelarTransaccion(id, cedula);

        if (!cancelada) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("No se pudo cancelar la transacción (puede ya estar ejecutada o no existir)");
        }

        return ResponseEntity.ok("Transacción cancelada correctamente");
    }

    @GetMapping("/listar/{cedula}")
    public ResponseEntity<?> listarProgramadas(@PathVariable String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        ArrayList<TransaccionProgramada> lista = usuario.getTransaccionesProgramadas();
        java.util.ArrayList<TransaccionProgramada> respuesta = new java.util.ArrayList<>();

        for (TransaccionProgramada t : lista) {
            respuesta.add(t);
        }

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/cola")
    public ResponseEntity<?> verCola() {

        PriorityQueue<TransaccionProgramada> cola = transaccionProgramadaService.getSistemaBilletera().getColaProgramadas();

        java.util.ArrayList<TransaccionProgramada> respuesta = new java.util.ArrayList<>();

        for (TransaccionProgramada t : cola) {
            respuesta.add(t);
        }

        return ResponseEntity.ok(respuesta);
    }
}