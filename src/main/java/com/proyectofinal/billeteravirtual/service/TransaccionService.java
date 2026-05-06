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
            double comision,
            TipoTransaccion tipo,
            boolean generarPuntos
    ) {
        Transaccion t = new Transaccion();
        t.setId(UUID.randomUUID().toString());
        t.setFecha(LocalDateTime.now());
        t.setTipo(tipo);
        t.setValor(valor);
        t.setComision(comision);

        t.setBilleteraOrigenId(origen != null ? origen.getId() : null);
        t.setBilleteraDestinoId(destino != null ? destino.getId() : null);

        t.setEstado(EstadoTransaccion.COMPLETADA);

        if (generarPuntos) {
            int puntos = calcularPuntos(valor, tipo, usuario.getNivel());
            t.setPuntosGenerados(puntos);

            usuario.setPuntos(usuario.getPuntos() + puntos);
            usuario.setPuntosAcumulados(usuario.getPuntosAcumulados() + puntos);

            usuarioService.actualizarNivelUsuario(usuario);
        } else {
            t.setPuntosGenerados(0);
        }

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

        registrarTransaccion(usuario,null, billetera, valor, 0, TipoTransaccion.RECARGA, true);

        return true;
    }

    public boolean retirar(String cedula, String idBilletera, double valor) {

        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return false;

        Billetera billetera = usuario.getBilleteras().get(idBilletera);
        if (billetera == null) return false;

        if (billetera.getSaldo() < valor) return false;

        billetera.setSaldo(billetera.getSaldo() - valor);

        registrarTransaccion(usuario, billetera, null, valor, 0, TipoTransaccion.RETIRO, true);

        return true;
    }

    public int transferir(String cedula, String idOrigen, String idDestino, double valor) {

        Usuario usuarioOrigen = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuarioOrigen == null) return 2;

        if (idOrigen.equals(idDestino)) return 3;

        Billetera origen = usuarioOrigen.getBilleteras().get(idOrigen);
        if (origen == null) return 2;

        Billetera destino = usuarioService.buscarBilleteraGlobal(idDestino);
        if (destino == null) return 2;

        Usuario usuarioDestino = usuarioService.buscarUsuarioPorBilletera(idDestino);
        if (usuarioDestino == null) return 2;

        if (valor <= 0) return 1;

        double porcentaje = obtenerComision(usuarioOrigen.getNivel());
        double comision = valor * porcentaje;
        double totalDescontar = valor + comision;

        if (origen.getSaldo() < totalDescontar) return 1;

        origen.setSaldo(origen.getSaldo() - totalDescontar);
        destino.setSaldo(destino.getSaldo() + valor);

        registrarTransaccion(usuarioOrigen, origen, destino, valor, comision, TipoTransaccion.TRANSFERENCIA, true);

        if (!usuarioOrigen.getCedula().equals(usuarioDestino.getCedula())) {
            registrarTransaccion(usuarioDestino, origen, destino, valor, comision, TipoTransaccion.TRANSFERENCIA, false);
        }

        return 4;
    }

    private double obtenerComision(NivelUsuario nivel) {
        return switch (nivel) {
            case BRONCE -> 0.005;
            case PLATA -> 0.004;
            case ORO -> 0.003;
            case PLATINO -> 0.001;
        };
    }
}
