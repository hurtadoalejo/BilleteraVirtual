package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransaccionService {
    private final UsuarioService usuarioService;
    private final EmailService emailService;

    public TransaccionService(UsuarioService usuarioService, EmailService emailService) {
        this.usuarioService = usuarioService;
        this.emailService = emailService;
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

        try {
            enviarCorreoTransaccion(usuario, TipoTransaccion.RECARGA, valor, null, billetera, null);
        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

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

        try {
            enviarCorreoTransaccion(usuario, TipoTransaccion.RETIRO, valor, billetera, null, null);
        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }

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

        try {
            enviarCorreoTransaccion(usuarioOrigen, TipoTransaccion.TRANSFERENCIA, valor, origen, destino, usuarioDestino);

            enviarCorreoTransferenciaRecibida(usuarioOrigen, usuarioDestino, valor);

        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
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

    public List<Transaccion> obtenerHistorial(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) return null;

        return usuario.getHistorialTransacciones();
    }

    private void enviarCorreoTransaccion(
            Usuario usuario,
            TipoTransaccion tipo,
            double valor,
            Billetera origen,
            Billetera destino,
            Usuario usuarioDestino
    ) {

        String asunto = "Notificación de transacción";

        String mensaje = "Hola " + usuario.getNombreCompleto() + ",\n\n";

        switch (tipo) {
            case RECARGA ->
                    mensaje += "Recargaste $" + valor +
                            " a tu billetera " + destino.getId() + ".";

            case RETIRO ->
                    mensaje += "Retiraste $" + valor +
                            " desde tu billetera " + origen.getId() + ".";

            case TRANSFERENCIA ->
                    mensaje += "Transferiste $" + valor +
                            " desde tu billetera " + origen.getId() +
                            " a " + usuarioDestino.getNombreCompleto() + ".";
        }

        mensaje += "\n\nGracias por usar Billetera Virtual \uD83D\uDC99";

        emailService.enviarCorreo(usuario.getCorreoElectronico(), asunto, mensaje);
    }

    private void enviarCorreoTransferenciaRecibida(Usuario origen, Usuario destino, double valor) {
        if (origen.getCedula().equals(destino.getCedula())) return;

        String asunto = "Transferencia recibida";

        String mensaje = "Hola " + destino.getNombreCompleto() + ",\n\n" +
                "Has recibido $" + valor + " de " + origen.getNombreCompleto() + ".\n\n" +
                "Gracias por usar Billetera Virtual.";

        emailService.enviarCorreo(destino.getCorreoElectronico(), asunto, mensaje);
    }
}
