package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.enums.NivelUsuario;
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

    /**
     * Registra un nuevo usuario en el sistema si no existe previamente.
     * @param usuario El objeto Usuario que se desea registrar.
     * @return true si el usuario se registró con éxito; false si es nulo o ya está registrado.
     */
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

    /**
     * Busca una billetera específica buscando en cada usuario.
     * @param idBilletera El identificador único de la billetera a buscar.
     * @return La Billetera encontrada, o null si no existe.
     */
    public Billetera buscarBilleteraGlobal(String idBilletera) {
        for (Usuario usuario : sistema.getUsuarios().values()) {
            if (usuario.getBilleteras().containsKey(idBilletera)) {
                return usuario.getBilleteras().get(idBilletera);
            }
        }

        return null;
    }

    /**
     * Busca al propietario de una billetera específica basándose en su ID.
     * @param idBilletera El identificador único de la billetera.
     * @return El Usuario dueño de la billetera, o null si no se encuentra.
     */
    public Usuario buscarUsuarioPorBilletera(String idBilletera) {
        for (Usuario usuario : sistema.getUsuarios().values()) {
            if (usuario.getBilleteras().containsKey(idBilletera)) {
                return usuario;
            }
        }

        return null;
    }

    /**
     * Agrega una transacción revertida al historial de reversiones de un usuario.
     * @param cedula El documento de identidad del usuario.
     * @param transaccion La transacción que fue revertida.
     */
    public void agregarHistorialReversiones(String cedula, Transaccion transaccion) {
        Usuario usuario = buscarUsuarioPorCedula(cedula);
        if (usuario == null) return;
        usuario.getHistorialRevertidas().push(transaccion);
    }

    /**
     * Busca un usuario en el sistema mediante su número de cédula.
     * @param cedula El documento de identidad del usuario a buscar.
     * @return El Usuario correspondiente, o null si no existe.
     */
    public Usuario buscarUsuarioPorCedula(String cedula) {
        return sistema.getUsuarios().get(cedula);
    }

    /**
     * Genera una lista con la información resumida y formateada de todos los usuarios.
     * @return Un ArrayList de objetos UsuarioResponse con los datos procesados.
     */
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

    /**
     * Actualiza los datos personales de un usuario existente en el sistema.
     * @param cedula El documento de identidad del usuario a modificar.
     * @param datosActualizados Objeto con los nuevos datos del usuario.
     * @return true si se actualizó correctamente; false si el usuario no existe.
     */
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

    /**
     * Elimina a un usuario del sistema y de los registros de ranking.
     * @param cedula El documento de identidad del usuario a eliminar.
     * @return true si el usuario fue eliminado con éxito; false si no existía.
     */
    public boolean eliminarUsuario(String cedula) {
        Usuario usuario = sistema.getUsuarios().remove(cedula);
        if (usuario == null) return false;
        sistema.getUsuariosPorPuntos().remove(usuario);

        return true;
    }

    /**
     * Actualiza el nivel (rango) de un usuario según sus puntos acumulados.
     * @param usuario El usuario al que se le evaluará y actualizará el nivel.
     */
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

    /**
     * Actualiza la posición de un usuario en la lista ordenada por puntos (ranking)
     * @param usuario El usuario cuya posición en el ranking se va a refrescar.
     */
    public void actualizarRankingUsuario(Usuario usuario) {
        sistema.getUsuariosPorPuntos().remove(usuario);
        sistema.getUsuariosPorPuntos().add(usuario);
    }
}