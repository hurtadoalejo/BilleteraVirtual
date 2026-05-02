package com.proyectofinal.billeteravirtual.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrincipalController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login.html";
    }
}