package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.response.UsuarioResponse;
import org.springframework.stereotype.Service;
import com.proyectofinal.billeteravirtual.util.ArrayList;

@Service
public class UsuarioService {

    private final SistemaBilletera sistema;

    public UsuarioService(SistemaBilletera sistema) {
        this.sistema = sistema;
    }

    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        if (sistema.getUsuarios().containsKey(usuario.getCedula())) {
            return false;
        }
        sistema.getUsuarios().put(usuario.getCedula(), usuario);
        sistema.getUsuariosPorPuntos().add(usuario);

        return true;
    }

    public Billetera buscarBilleteraGlobal(String idBilletera) {
        for (Usuario usuario : sistema.getUsuarios().values()) {
            if (usuario.getBilleteras().containsKey(idBilletera)) {
                return usuario.getBilleteras().get(idBilletera);
            }
        }

        return null;
    }

    public Usuario buscarUsuarioPorBilletera(String idBilletera) {
        for (Usuario usuario : sistema.getUsuarios().values()) {
            if (usuario.getBilleteras().containsKey(idBilletera)) {
                return usuario;
            }
        }

        return null;
    }

    public void agregarHistorialReversiones(String cedula, Transaccion transaccion) {
        Usuario usuario = buscarUsuarioPorCedula(cedula);
        if (usuario == null) return;
        usuario.getHistorialRevertidas().push(transaccion);
    }

    public Usuario buscarUsuarioPorCedula(String cedula) {
        return sistema.getUsuarios().get(cedula);
    }

    public java.util.ArrayList<UsuarioResponse> listarUsuarios() {
        java.util.ArrayList<UsuarioResponse> lista = new java.util.ArrayList<>();

        for (Usuario usuario : sistema.getUsuarios().values()) {
            UsuarioResponse response = new UsuarioResponse();
            response.setNombreCompleto(usuario.getNombreCompleto());
            response.setCedula(usuario.getCedula());
            response.setNivel(usuario.getNivel());
            response.setPuntos(usuario.getPuntosAcumulados());

            double saldoTotal = 0;
            for (Billetera billetera : usuario.getBilleteras().values()) {
                saldoTotal += billetera.getSaldo();
            }

            response.setSaldoTotal(saldoTotal);
            java.util.ArrayList<Transaccion> historial = new java.util.ArrayList<>();
            for (Transaccion transaccion : usuario.getHistorialTransacciones()) {
                historial.add(transaccion);
            }

            response.setHistorialTransacciones(historial);

            lista.add(response);
        }

        return lista;
    }

    public boolean actualizarUsuario(String cedula, Usuario datosActualizados) {
        Usuario usuario = sistema.getUsuarios().get(cedula);
        if (usuario == null) {
            return false;
        }

        usuario.setNombreCompleto(datosActualizados.getNombreCompleto());
        usuario.setCorreoElectronico(datosActualizados.getCorreoElectronico());
        usuario.setNumeroTelefonico(datosActualizados.getNumeroTelefonico());
        usuario.setPassword(datosActualizados.getPassword());

        return true;
    }

    public boolean eliminarUsuario(String cedula) {
        Usuario usuario = sistema.getUsuarios().remove(cedula);
        if (usuario == null) return false;
        sistema.getUsuariosPorPuntos().remove(usuario);

        return true;
    }

    public void actualizarNivelUsuario(Usuario usuario) {
        int puntos = usuario.getPuntosAcumulados();

        if (puntos <= 500) {
            usuario.setNivel(NivelUsuario.BRONCE);

        } else if (puntos <= 1000) {
            usuario.setNivel(NivelUsuario.PLATA);

        } else if (puntos <= 5000) {
            usuario.setNivel(NivelUsuario.ORO);

        } else {
            usuario.setNivel(NivelUsuario.PLATINO);
        }
    }

    public void actualizarRankingUsuario(Usuario usuario) {
        sistema.getUsuariosPorPuntos().remove(usuario);
        sistema.getUsuariosPorPuntos().add(usuario);
    }

    public ArrayList<Usuario> obtenerRankingUsuarios() {
        ArrayList<Usuario> lista = new ArrayList<>();
        for (Usuario usuario : sistema.getUsuariosPorPuntos()) {
            lista.add(usuario);
        }

        return lista;
    }
}