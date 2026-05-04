package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransaccionService {
    private final UsuarioService usuarioService;

    public TransaccionService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private int calcularPuntos(double valor, TipoTransaccion tipo, NivelUsuario nivel) {

        int base = 0;

        switch (tipo) {
            case RECARGA -> base = 1;
            case RETIRO -> base = 2;
            case TRANSFERENCIA -> base = 3;
        }

        int bonus = switch (nivel) {
            case BRONCE -> 0;
            case PLATA -> 1;
            case ORO -> 2;
            case PLATINO -> 3;
        };

        return (int) (valor / 5000) * (base + bonus);
    }

    private void registrarTransaccion(
            Usuario usuario,
            Billetera origen,
            Billetera destino,
            double valor,
            TipoTransaccion tipo
    ) {
        Transaccion t = new Transaccion();
        t.setId(UUID.randomUUID().toString());
        t.setFecha(LocalDateTime.now());
        t.setTipo(tipo);
        t.setValor(valor);

        t.setBilleteraOrigenId(origen != null ? origen.getId() : null);
        t.setBilleteraDestinoId(destino != null ? destino.getId() : null);

        t.setEstado(EstadoTransaccion.COMPLETADA);

        int puntos = calcularPuntos(valor, tipo, usuario.getNivel());
        t.setPuntosGenerados(puntos);

        usuario.setPuntos(usuario.getPuntos() + puntos);
        usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() + puntos);

        usuarioService.actualizarNivelUsuario(usuario);

        if (origen != null) {
            origen.getTransacciones().add(t);
        }

        if (destino != null && destino != origen) {
            destino.getTransacciones().add(t);
        }

        usuario.getHistorialTransacciones().add(t);
    }

    public boolean recargar(String cedula, String idBilletera, double valor) {

        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return false;

        Billetera billetera = usuario.getBilleteras().get(idBilletera);
        if (billetera == null) return false;

        billetera.setSaldo(billetera.getSaldo() + valor);

        registrarTransaccion(usuario,null, billetera, valor, TipoTransaccion.RECARGA);

        return true;
    }

    public boolean retirar(String cedula, String idBilletera, double valor) {

        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return false;

        Billetera billetera = usuario.getBilleteras().get(idBilletera);
        if (billetera == null) return false;

        if (billetera.getSaldo() < valor) return false;

        billetera.setSaldo(billetera.getSaldo() - valor);

        registrarTransaccion(usuario, billetera, null, valor, TipoTransaccion.RETIRO);

        return true;
    }
}
