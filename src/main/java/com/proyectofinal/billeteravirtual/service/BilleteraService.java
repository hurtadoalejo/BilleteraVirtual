package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.response.BilleteraResponse;
import org.springframework.stereotype.Service;
import com.proyectofinal.billeteravirtual.util.ArrayList;

import java.util.Map;

@Service
public class BilleteraService {

    private final UsuarioService usuarioService;
    private final SistemaService sistemaService;
    private final SistemaBilletera sistema;
    private static int contadorId = 0;

    public BilleteraService(UsuarioService usuarioService, SistemaService sistemaService, SistemaBilletera sistema) {
        this.usuarioService = usuarioService;
        this.sistemaService = sistemaService;
        this.sistema = sistema;
    }

    public boolean agregarBilletera(String cedula, Billetera billetera) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null || billetera == null) return false;

        Map<String, Billetera> billeteras = usuario.getBilleteras();
        billetera.setId("018" + String.format("%03d", contadorId++));
        billetera.setUsuario(usuario);

        billeteras.put(billetera.getId(), billetera);
        sistema.getBilleterasPorSaldo().add(billetera);

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

        if (billetera.getSaldo() > 0) return false;
        usuario.getBilleteras().remove(idBilletera);
        sistema.getBilleterasPorSaldo().remove(billetera);
        return true;
    }

    public java.util.ArrayList<BilleteraResponse> listarBilleterasAdmin() {
        java.util.ArrayList<BilleteraResponse> lista = new java.util.ArrayList<>();
        for (Usuario usuario : sistemaService.obtenerUsuarios()) {
            for (Billetera billetera : usuario.getBilleteras().values()) {
                BilleteraResponse response = new BilleteraResponse();

                response.setId(billetera.getId());
                response.setNombre(billetera.getNombre());
                response.setTipo(billetera.getTipo());
                response.setEstado(billetera.getEstado());
                response.setSaldo(billetera.getSaldo());

                response.setUsuarioNombre(usuario.getNombreCompleto());
                response.setUsuarioId(usuario.getCedula());
                response.setMovimientos(billetera.getTransacciones().size());

                lista.add(response);
            }
        }

        return lista;
    }

    public void actualizarSaldo(Billetera billetera, double nuevoSaldo) {
        sistema.getBilleterasPorSaldo().remove(billetera);
        billetera.setSaldo(nuevoSaldo);
        sistema.getBilleterasPorSaldo().add(billetera);
    }
}