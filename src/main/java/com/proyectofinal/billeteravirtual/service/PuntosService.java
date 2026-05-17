package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.enums.NivelUsuario;
import com.proyectofinal.billeteravirtual.enums.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class PuntosService {

    private final UsuarioService usuarioService;

    public PuntosService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Calcula la cantidad de puntos que genera una transacción según su tipo, monto y el nivel del usuario.
     * @param valor El monto total de la operación financiera.
     * @param tipo El tipo de transacción (RECARGA, RETIRO, TRANSFERENCIA).
     * @param nivel El rango actual del usuario (BRONCE, PLATA, ORO, PLATINO).
     * @return El total de puntos enteros calculados para la operación.
     */
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

    /**
     * Suma los puntos obtenidos al saldo actual y acumulado del usuario, actualizando su posición en el ranking y nivel.
     * @param usuario El usuario al que se le asignarán los puntos.
     * @param puntos La cantidad de puntos a añadir.
     */
    public void aplicarPuntos(Usuario usuario, int puntos) {
        usuario.setPuntos(usuario.getPuntos() + puntos);

        usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() + puntos);

        usuarioService.actualizarRankingUsuario(usuario);
        usuarioService.actualizarNivelUsuario(usuario);
    }

    /**
     * Resta puntos al saldo actual y acumulado del usuario debido a una cancelación o reversión, previniendo valores negativos.
     * @param usuario El usuario al que se le removerán los puntos.
     * @param puntos La cantidad de puntos a descontar.
     */
    public void removerPuntos(Usuario usuario, int puntos) {
        usuario.setPuntos(usuario.getPuntos() - puntos);
        usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() - puntos);

        if (usuario.getPuntos() < 0) usuario.setPuntos(0);
        if (usuario.getPuntosAcumulados() < 0) usuario.setPuntosAcumulados(0);

        usuarioService.actualizarRankingUsuario(usuario);
        usuarioService.actualizarNivelUsuario(usuario);
    }
}