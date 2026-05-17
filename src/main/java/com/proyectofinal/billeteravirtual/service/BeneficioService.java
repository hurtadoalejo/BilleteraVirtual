package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.model.Beneficio;
import com.proyectofinal.billeteravirtual.model.Billetera;
import com.proyectofinal.billeteravirtual.model.Usuario;
import com.proyectofinal.billeteravirtual.util.ArrayList;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BeneficioService {

    private final UsuarioService usuarioService;
    private final BilleteraService billeteraService;

    public BeneficioService(UsuarioService usuarioService, BilleteraService billeteraService) {
        this.usuarioService = usuarioService;
        this.billeteraService = billeteraService;
    }

    /**
     * Realiza el canje de puntos acumulados por dinero asignado a una billetera específica.
     * @param cedula El documento de identidad del usuario.
     * @param idBilletera El identificador de la billetera destino.
     * @param puntos La cantidad de puntos que se desean canjear.
     * @return El objeto Beneficio generado con el detalle del canje, o null si los datos son inválidos o no hay puntos suficientes.
     */
    public Beneficio canjear(String cedula, String idBilletera, int puntos) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) return null;
        if (puntos <= 0) return null;
        if (usuario.getPuntos() < puntos) return null;

        Billetera billetera = billeteraService.buscarBilletera(cedula, idBilletera);
        if (billetera == null) return null;

        double dinero = puntos * 5;
        usuario.setPuntos(usuario.getPuntos() - puntos);
        billetera.setSaldo(billetera.getSaldo() + dinero);
        Beneficio beneficio = new Beneficio();
        beneficio.setId(UUID.randomUUID().toString());
        beneficio.setCostoPuntos(puntos);
        beneficio.setDineroCanjeado(dinero);
        beneficio.setFecha(LocalDateTime.now());
        beneficio.setBilleteraDestino(billetera);
        usuario.getListaBeneficios().add(beneficio);

        return beneficio;
    }

    /**
     * Obtiene el historial completo de los beneficios canjeados por un usuario.
     * @param cedula El documento de identidad del usuario.
     * @return Un ArrayList con los beneficios obtenidos, o una lista vacía si el usuario no existe.
     */
    public ArrayList<Beneficio> obtenerHistorial(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);
        if (usuario == null) return new ArrayList<>();
        return usuario.getListaBeneficios();
    }
}