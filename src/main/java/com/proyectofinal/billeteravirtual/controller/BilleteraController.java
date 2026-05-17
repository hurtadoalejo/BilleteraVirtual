package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.model.Billetera;
import com.proyectofinal.billeteravirtual.response.BilleteraResponse;
import com.proyectofinal.billeteravirtual.service.BilleteraService;
import com.proyectofinal.billeteravirtual.util.ArrayList;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/billeteras")
public class BilleteraController {

    private final BilleteraService billeteraService;

    public BilleteraController(BilleteraService billeteraService) {
        this.billeteraService = billeteraService;
    }

    @PostMapping("/{cedula}")
    public ResponseEntity<?> crearBilletera(@PathVariable String cedula, @RequestBody Billetera billetera) {

        boolean creada = billeteraService.agregarBilletera(cedula, billetera);

        if (!creada) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("No se pudo crear la billetera");
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Billetera creada correctamente");
    }

    @GetMapping("/{cedula}")
    public ResponseEntity<?> listarBilleteras(@PathVariable String cedula) {

        ArrayList<Billetera> billeterasPropias = billeteraService.listarBilleteras(cedula);
        java.util.ArrayList<Billetera> billeteras = new java.util.ArrayList<>();
        for (Billetera billetera : billeterasPropias) {
            billeteras.add(billetera);
        }

        return ResponseEntity.ok(billeteras);
    }

    @GetMapping("/{cedula}/{idBilletera}")
    public ResponseEntity<?> buscarBilletera(@PathVariable String cedula, @PathVariable String idBilletera) {

        Billetera billetera = billeteraService.buscarBilletera(cedula, idBilletera);

        if (billetera == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Billetera no encontrada");
        }

        return ResponseEntity.ok(billetera);
    }

    @PutMapping("/{cedula}/{idBilletera}")
    public ResponseEntity<?> actualizarBilletera(@PathVariable String cedula, @PathVariable String idBilletera, @RequestBody Billetera datos) {

        boolean actualizada = billeteraService.actualizarBilletera(cedula, idBilletera, datos);

        if (!actualizada) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Billetera no encontrada");
        }

        return ResponseEntity.ok("Billetera actualizada correctamente");
    }

    @DeleteMapping("/{cedula}/{idBilletera}")
    public ResponseEntity<?> eliminarBilletera(@PathVariable String cedula, @PathVariable String idBilletera) {

        boolean eliminada = billeteraService.eliminarBilletera(cedula, idBilletera);

        if (!eliminada) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar la billetera, debe tener saldo 0");
        }

        return ResponseEntity.ok("Billetera eliminada correctamente");
    }

    @GetMapping("/admin")
    public ResponseEntity<java.util.ArrayList<BilleteraResponse>> listarBilleteras() {
        return ResponseEntity.ok(billeteraService.listarBilleterasAdmin());
    }
}