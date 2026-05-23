package com.proyectofinal.billeteravirtual.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatClienteController {

    private final ChatClient chatClient;

    public ChatClienteController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String mensaje) {

        return chatClient
                .prompt()
                .user(mensaje)
                .call()
                .content();
    }
}