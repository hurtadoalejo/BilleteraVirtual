package com.proyectofinal.billeteravirtual.model;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Service
public class SistemaBilletera {
    private Map<String, Usuario> usuarios = new HashMap<>();
    private PriorityQueue<TransaccionProgramada> colaProgramadas = new PriorityQueue<>();

    public Map<String, Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Map<String, Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public PriorityQueue<TransaccionProgramada> getColaProgramadas() {
        return colaProgramadas;
    }

    public void setColaProgramadas(PriorityQueue<TransaccionProgramada> colaProgramadas) {
        this.colaProgramadas = colaProgramadas;
    }
}
