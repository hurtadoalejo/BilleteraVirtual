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

    /**
     * Crea una nueva billetera para un usuario
     * @param cedula cédula del usuario
     * @param billetera datos de la billetera a crear
     * @return mensaje de estado de la operación
     */
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

    /**
     * Lista todas las billeteras de un usuario
     * @param cedula cédula del usuario
     * @return lista de billeteras
     */
    @GetMapping("/{cedula}")
    public ResponseEntity<?> listarBilleteras(@PathVariable String cedula) {

        ArrayList<Billetera> billeterasPropias = billeteraService.listarBilleteras(cedula);
        java.util.ArrayList<Billetera> billeteras = new java.util.ArrayList<>();
        for (Billetera billetera : billeterasPropias) {
            billeteras.add(billetera);
        }

        return ResponseEntity.ok(billeteras);
    }

    /**
     * Busca una billetera específica de un usuario
     * @param cedula cédula del usuario
     * @param idBilletera identificador de la billetera
     * @return billetera encontrada o error 404
     */
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

    /**
     * Actualiza una billetera existente
     * @param cedula cédula del usuario
     * @param idBilletera identificador de la billetera
     * @param datos nuevos datos de la billetera
     * @return mensaje de estado de la operación
     */
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

    /**
     * Elimina una billetera si cumple las condiciones
     * @param cedula cédula del usuario
     * @param idBilletera identificador de la billetera
     * @return mensaje de estado de la operación
     */
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

    /**
     * Lista todas las billeteras del sistema (admin)
     * @return lista de billeteras
     */
    @GetMapping("/admin")
    public ResponseEntity<java.util.ArrayList<BilleteraResponse>> listarBilleteras() {
        return ResponseEntity.ok(billeteraService.listarBilleterasAdmin());
    }
}