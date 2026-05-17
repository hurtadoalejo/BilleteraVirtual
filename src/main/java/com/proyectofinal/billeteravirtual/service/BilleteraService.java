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

    /**
     * Agrega una nueva billetera a un usuario asignándole un identificador único correlativo.
     * @param cedula El documento de identidad del usuario.
     * @param billetera El objeto Billetera que se va a asociar y registrar.
     * @return true si la billetera fue agregada y vinculada con éxito; false si el usuario o la billetera son nulos.
     */
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

    /**
     * Busca una billetera específica dentro de la colección del usuario indicado.
     * @param cedula El documento de identidad del usuario.
     * @param idBilletera El identificador único de la billetera a buscar.
     * @return La Billetera encontrada, o null si el usuario no existe o no posee dicha billetera.
     */
    public Billetera buscarBilletera(String cedula, String idBilletera) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) return null;

        return usuario.getBilleteras().get(idBilletera);
    }

    /**
     * Recupera todas las billeteras asociadas a un usuario en formato de lista.
     * @param cedula El documento de identidad del usuario.
     * @return Un ArrayList con las billeteras del usuario, o una lista vacía si el usuario no existe.
     */
    public ArrayList<Billetera> listarBilleteras(String cedula) {
        Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

        if (usuario == null) return new ArrayList<>();

        ArrayList<Billetera> lista = new ArrayList<>();
        for (Billetera b : usuario.getBilleteras().values()) {
            lista.add(b);
        }

        return lista;
    }

    /**
     * Modifica los datos básicos (nombre, tipo y estado) de una billetera existente.
     * @param cedula El documento de identidad del usuario propietario.
     * @param idBilletera El identificador único de la billetera a modificar.
     * @param datos Objeto Billetera que contiene la nueva información a aplicar.
     * @return true si los datos se actualizaron correctamente; false si la billetera no fue encontrada.
     */
    public boolean actualizarBilletera(String cedula, String idBilletera, Billetera datos) {
        Billetera billetera = buscarBilletera(cedula, idBilletera);

        if (billetera == null) return false;

        billetera.setNombre(datos.getNombre());
        billetera.setTipo(datos.getTipo());
        billetera.setEstado(datos.getEstado());

        return true;
    }

    /**
     * Elimina una billetera del usuario y del sistema, siempre y cuando su saldo sea cero.
     * @param cedula El documento de identidad del usuario propietario.
     * @param idBilletera El identificador único de la billetera a remover.
     * @return true si la billetera se eliminó con éxito; false si no existe o si aún conserva saldo disponible.
     */
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

    /**
     * Genera un reporte detallado de todas las billeteras globales del sistema para fines administrativos.
     * @return Un ArrayList de objetos BilleteraResponse con información consolidada del usuario y rendimiento de la billetera.
     */
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

    /**
     * Modifica el saldo de una billetera y reorganiza su posición en la estructura de ordenamiento por saldos.
     * @param billetera El objeto Billetera que sufrirá el cambio de saldo.
     * @param nuevoSaldo El nuevo valor monetario que se asignará a la billetera.
     */
    public void actualizarSaldo(Billetera billetera, double nuevoSaldo) {
        sistema.getBilleterasPorSaldo().remove(billetera);
        billetera.setSaldo(nuevoSaldo);
        sistema.getBilleterasPorSaldo().add(billetera);
    }
}