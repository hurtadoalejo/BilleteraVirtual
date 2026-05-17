package com.proyectofinal.billeteravirtual.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrincipalController {

    /**
     * Direcciona automáticamente en el localhost a login.html
     * @return redirección a la página de login
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/login.html";
    }
}