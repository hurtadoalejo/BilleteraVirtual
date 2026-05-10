package com.proyectofinal.billeteravirtual.model;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

@Service
public class SistemaBilletera {
    private Map<String, Usuario> usuarios = new HashMap<>();
    private PriorityQueue<TransaccionProgramada> colaProgramadas = new PriorityQueue<>();
    private TreeSet<Usuario> usuariosPorPuntos = new TreeSet<>();

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

    public TreeSet<Usuario> getUsuariosPorPuntos() {
        return usuariosPorPuntos;
    }

    public void setUsuariosPorPuntos(TreeSet<Usuario> usuariosPorPuntos) {
        this.usuariosPorPuntos = usuariosPorPuntos;
    }
}
