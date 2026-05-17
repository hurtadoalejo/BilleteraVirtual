package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.model.Usuario;

import com.proyectofinal.billeteravirtual.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Registra un nuevo usuario en el sistema
     * @param usuario datos del usuario a registrar
     * @return mensaje de estado de la operación
     */
    @PostMapping
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {

        boolean creado = usuarioService.registrarUsuario(usuario);
        if (!creado) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("El usuario ya existe");
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Usuario registrado correctamente");
    }

    /**
     * Busca un usuario por su cédula
     * @param cedula cédula del usuario
     * @return usuario encontrado o mensaje de error
     */
    @GetMapping("/{cedula}")
    public ResponseEntity<?> buscarUsuario(@PathVariable String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }

        return ResponseEntity.ok(usuario);
    }

    /**
     * Actualiza la información de un usuario existente
     * @param cedula cédula del usuario
     * @param usuarioActualizado nuevos datos del usuario
     * @return mensaje de estado de la operación
     */
    @PutMapping("/{cedula}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable String cedula, @RequestBody Usuario usuarioActualizado) {
        boolean actualizado = usuarioService.actualizarUsuario(cedula, usuarioActualizado);

        if (!actualizado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }

        return ResponseEntity.ok("Usuario actualizado correctamente");
    }

    /**
     * Elimina un usuario del sistema
     * @param cedula cédula del usuario
     * @return mensaje de estado de la operación
     */
    @DeleteMapping("/{cedula}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable String cedula) {
        boolean eliminado = usuarioService.eliminarUsuario(cedula);

        if (!eliminado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }

        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    /**
     * Autentica un usuario en el sistema
     * @param usuario credenciales del usuario
     * @return usuario autenticado o error de credenciales
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        Usuario encontrado = usuarioService.buscarUsuarioPorCedula(usuario.getCedula());

        if (encontrado != null &&
                encontrado.getPassword().equals(usuario.getPassword())) {
            return ResponseEntity.ok(encontrado);
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Credenciales incorrectas");
    }

    /**
     * Lista todos los usuarios del sistema
     * @return lista de usuarios
     */
    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }
}