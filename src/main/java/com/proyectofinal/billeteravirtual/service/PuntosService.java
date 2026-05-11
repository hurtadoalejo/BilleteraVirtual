package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.NivelUsuario;
import com.proyectofinal.billeteravirtual.model.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class PuntosService {

    private final UsuarioService usuarioService;

    public PuntosService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public int calcularPuntos(double valor, TipoTransaccion tipo, NivelUsuario nivel) {
        int base = switch (tipo) {
            case RECARGA -> 1;
            case RETIRO -> 2;
            case TRANSFERENCIA -> 3;
        };

        int bonus = switch (nivel) {
            case BRONCE -> 0;
            case PLATA -> 1;
            case ORO -> 2;
            case PLATINO -> 3;
        };

        return (int) (valor / 5000) * (base + bonus);
    }

    public void aplicarPuntos(Usuario usuario, int puntos) {

        usuario.setPuntos(usuario.getPuntos() + puntos);

        usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() + puntos);

        usuarioService.actualizarRankingUsuario(usuario);
        usuarioService.actualizarNivelUsuario(usuario);
    }

    public void removerPuntos(Usuario usuario, int puntos) {
        usuario.setPuntos(usuario.getPuntos() - puntos);
        usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() - puntos);

        if (usuario.getPuntos() < 0) usuario.setPuntos(0);
        if (usuario.getPuntosAcumulados() < 0) usuario.setPuntosAcumulados(0);

        usuarioService.actualizarRankingUsuario(usuario);
        usuarioService.actualizarNivelUsuario(usuario);
    }
}