package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.Billetera;
import com.proyectofinal.billeteravirtual.model.NivelUsuario;
import com.proyectofinal.billeteravirtual.model.SistemaBilletera;
import com.proyectofinal.billeteravirtual.model.Usuario;
import org.springframework.stereotype.Service;
import com.proyectofinal.billeteravirtual.util.ArrayList;

@Service
public class UsuarioService {

    private final SistemaBilletera sistema;

    public UsuarioService(SistemaBilletera sistema) {
        this.sistema = sistema;

        Usuario usuario = new Usuario();

        usuario.setNombreCompleto("Alejandro Hurtado");
        usuario.setCedula("1092850037");
        usuario.setCorreoElectronico("alejohg2911@gmail.com");
        usuario.setNumeroTelefonico("3161971519");
        usuario.setPassword("alejohg");

        registrarUsuario(usuario);
    }

    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        if (sistema.getUsuarios().containsKey(usuario.getCedula())) {
            return false;
        }
        sistema.getUsuarios().put(usuario.getCedula(), usuario);

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

    public Usuario buscarUsuarioPorCedula(String cedula) {
        return sistema.getUsuarios().get(cedula);
    }

    public ArrayList<Usuario> listarUsuarios() {
        ArrayList<Usuario> lista = new ArrayList<>();

        for (Usuario usuario : sistema.getUsuarios().values()) {
            lista.add(usuario);
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
        return sistema.getUsuarios().remove(cedula) != null;
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
}