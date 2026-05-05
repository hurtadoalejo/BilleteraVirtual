package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.Billetera;
import com.proyectofinal.billeteravirtual.model.NivelUsuario;
import com.proyectofinal.billeteravirtual.model.TipoBilletera;
import com.proyectofinal.billeteravirtual.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    private Map<String, Usuario> usuarios = new HashMap<>();

    public UsuarioService() {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Usuario");
        usuario.setCedula("123");
        usuario.setCorreoElectronico("123");
        usuario.setNumeroTelefonico("123");
        usuario.setPassword("123");
        Billetera billetera = new Billetera();
        billetera.setId("123");
        billetera.setNombre("Bille");
        billetera.setTipo(TipoBilletera.AHORRO);
        billetera.setSaldo(45000);
        usuario.getBilleteras().put(billetera.getId(), billetera);
        usuarios.put(usuario.getCedula(), usuario);
    }

    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null) return false;

        if (usuarios.containsKey(usuario.getCedula())) {
            return false;
        }

        usuarios.put(usuario.getCedula(), usuario);
        return true;
    }

    public Billetera buscarBilleteraGlobal(String idBilletera) {

        for (Usuario usuario : usuarios.values()) {
            if (usuario.getBilleteras().containsKey(idBilletera)) {
                return usuario.getBilleteras().get(idBilletera);
            }
        }

        return null;
    }

    public Usuario buscarUsuarioPorBilletera(String idBilletera) {

        for (Usuario usuario : usuarios.values()) {
            if (usuario.getBilleteras().containsKey(idBilletera)) {
                return usuario;
            }
        }

        return null;
    }

    public Usuario buscarUsuarioPorCedula(String cedula) {
        return usuarios.get(cedula);
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios.values());
    }

    public boolean actualizarUsuario(String cedula, Usuario datosActualizados) {
        Usuario usuario = usuarios.get(cedula);

        if (usuario == null) return false;

        usuario.setNombreCompleto(datosActualizados.getNombreCompleto());
        usuario.setCorreoElectronico(datosActualizados.getCorreoElectronico());
        usuario.setNumeroTelefonico(datosActualizados.getNumeroTelefonico());
        usuario.setPassword(datosActualizados.getPassword());

        return true;
    }

    public boolean eliminarUsuario(String cedula) {
        return usuarios.remove(cedula) != null;
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