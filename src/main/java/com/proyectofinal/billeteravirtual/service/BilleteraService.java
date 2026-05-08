package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import org.springframework.stereotype.Service;
import com.proyectofinal.billeteravirtual.util.ArrayList;

import java.util.Map;

@Service
public class BilleteraService {

    private final UsuarioService usuarioService;
    private static int contadorId = 0;

    public BilleteraService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        Billetera billetera = new Billetera();
        billetera.setNombre("Nequi");
        billetera.setTipo(TipoBilletera.AHORRO);
        agregarBilletera("1092850037", billetera);
    }

    public boolean agregarBilletera(String cedula, Billetera billetera) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null || billetera == null) return false;

        Map<String, Billetera> billeteras = usuario.getBilleteras();

        billetera.setId("018" + String.format("%03d", contadorId++));

        billeteras.put(billetera.getId(), billetera);

        return true;
    }

    public Billetera buscarBilletera(String cedula, String idBilletera) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) return null;

        return usuario.getBilleteras().get(idBilletera);
    }

    public ArrayList<Billetera> listarBilleteras(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) return new ArrayList<>();

        ArrayList<Billetera> lista = new ArrayList<>();
        for (Billetera b : usuario.getBilleteras().values()) {
            lista.add(b);
        }

        return lista;
    }

    public boolean actualizarBilletera(String cedula, String idBilletera, Billetera datos) {
        Billetera billetera = buscarBilletera(cedula, idBilletera);

        if (billetera == null) return false;

        billetera.setNombre(datos.getNombre());
        billetera.setTipo(datos.getTipo());
        billetera.setEstado(datos.getEstado());

        return true;
    }

    public boolean eliminarBilletera(String cedula, String idBilletera) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) return false;

        Billetera billetera = usuario.getBilleteras().get(idBilletera);

        if (billetera == null) return false;

        if (billetera.getSaldo() > 0) {
            return false;
        }

        usuario.getBilleteras().remove(idBilletera);
        return true;
    }
}