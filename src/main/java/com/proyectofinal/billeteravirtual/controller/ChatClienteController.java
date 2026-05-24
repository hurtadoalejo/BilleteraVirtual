package com.proyectofinal.billeteravirtual.controller;

import com.proyectofinal.billeteravirtual.service.ChatClienteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatClienteController {

    private final ChatClienteService chatClienteService;

    public ChatClienteController(ChatClienteService chatClienteService) {
        this.chatClienteService = chatClienteService;
    }

    /**
     * Atiende las consultas del usuario para la billetera virtual.
     * @param mensaje Texto de la consulta (mínimo 2 caracteres).
     * @return Respuesta del asistente o saludo predeterminado si el mensaje es inválido.
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String mensaje) {

        if (mensaje == null || mensaje.trim().length() < 2) {
            return "¿En qué puedo ayudarte con la billetera virtual?";
        }

        return chatClienteService.responder(mensaje);
    }
}