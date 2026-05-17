package com.proyectofinal.billeteravirtual.service;

import com.proyectofinal.billeteravirtual.enums.EstadoTransaccion;
import com.proyectofinal.billeteravirtual.enums.NivelRiesgo;
import com.proyectofinal.billeteravirtual.enums.TipoTransaccion;
import com.proyectofinal.billeteravirtual.model.Billetera;
import com.proyectofinal.billeteravirtual.model.SistemaBilletera;
import com.proyectofinal.billeteravirtual.model.Transaccion;
import com.proyectofinal.billeteravirtual.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class FraudeService {
    private final SistemaService sistemaService;

    public FraudeService(SistemaService sistemaService) {
        this.sistemaService = sistemaService;
    }

    /**
     * Orquesta el análisis de seguridad de una transacción ejecutando múltiples reglas de negocio
     * orientadas a la detección de fraudes y patrones de riesgo.
     * @param usuario El usuario que realiza la transacción.
     * @param transaccion La transacción que se va a auditar.
     */
    public void analizarTransaccion(Usuario usuario, Transaccion transaccion) {
        if (usuario == null || transaccion == null) return;

        detectarTransaccionesConsecutivas(usuario, transaccion);
        detectarMontoInusual(usuario, transaccion);
        detectarDestinoRepetido(usuario, transaccion);
        detectarFragmentacionMontos(usuario, transaccion);
        detectarFrecuenciaInusual(usuario, transaccion);
        detectarHorarioInusual(usuario, transaccion);
        actualizarNivelRiesgo(transaccion);
    }

    /**
     * Evalúa si existen 3 o más transacciones exitosas completadas en un intervalo de tiempo
     * igual o menor a 30 segundos respecto a la transacción actual.
     * @param usuario El usuario bajo evaluación.
     * @param actual La transacción en proceso de auditoría.
     */
    private void detectarTransaccionesConsecutivas(Usuario usuario, Transaccion actual) {

        int contador = 0;

        for (Transaccion t : usuario.getHistorialTransacciones()) {
            if (t == actual) continue;
            if (t.getEstado() != EstadoTransaccion.COMPLETADA) continue;

            long segundos = Duration.between(t.getFecha(), actual.getFecha()).getSeconds();

            if (Math.abs(segundos) <= 30) {
                contador++;
            }
        }

        if (contador >= 3) {
            registrarAlerta(actual, "Múltiples transacciones consecutivas en poco tiempo");
        }
    }

    /**
     * Dispara una alerta si el valor de la transacción actual supera en 5 veces o más
     * el promedio histórico de los movimientos completados por el usuario.
     * @param usuario El usuario emisor de los fondos.
     * @param actual La transacción financiera a evaluar.
     */
    private void detectarMontoInusual(Usuario usuario, Transaccion actual) {

        double promedio = calcularPromedioTransacciones(usuario, actual);

        if (promedio <= 0) {
            return;
        }

        if (actual.getValor() >= promedio * 5) {
            registrarAlerta(actual, "Monto inusualmente alto respecto al promedio del usuario");
        }
    }

    /**
     * Identifica ráfagas de transferencias dirigidas a la misma cuenta destino (3 o más
     * movimientos en menos de 10 minutos).
     * @param usuario El usuario emisor.
     * @param actual La transferencia bajo análisis.
     */
    private void detectarDestinoRepetido(Usuario usuario, Transaccion actual) {

        if (actual.getTipo() != TipoTransaccion.TRANSFERENCIA) {
            return;
        }

        int contador = 0;

        for (Transaccion t : usuario.getHistorialTransacciones()) {
            if (t == actual) continue;
            if (t.getEstado() != EstadoTransaccion.COMPLETADA) continue;
            if (t.getTipo() != TipoTransaccion.TRANSFERENCIA) continue;
            if (t.getBilleteraDestinoId() == null || actual.getBilleteraDestinoId() == null) continue;

            boolean mismoDestino = t.getBilleteraDestinoId().equals(actual.getBilleteraDestinoId());

            long minutos = Math.abs(Duration.between(t.getFecha(), actual.getFecha()).toMinutes());

            if (mismoDestino && minutos <= 10) {
                contador++;
            }
        }

        if (contador >= 3) {
            registrarAlerta(actual, "Transferencias repetidas al mismo destino en poco tiempo");
        }
    }

    /**
     * Alerta sobre la posible división de un capital grande en transacciones menores (pitufeo/smurfing),
     * validando si se registran 4 o más giros que sumen 1.000.000 o más en un lapso de 15 minutos.
     * @param usuario El usuario propietario de las billeteras inspeccionadas.
     * @param actual La transferencia en curso.
     */
    private void detectarFragmentacionMontos(Usuario usuario, Transaccion actual) {

        if (actual.getTipo() != TipoTransaccion.TRANSFERENCIA) {
            return;
        }

        double suma = 0;
        int contador = 0;

        for (Billetera billetera : usuario.getBilleteras().values()) {

            for (Transaccion t : billetera.getTransacciones()) {
                if (t == actual) continue;
                if (t.getTipo() != TipoTransaccion.TRANSFERENCIA) continue;
                if (t.getEstado() != EstadoTransaccion.COMPLETADA) continue;

                long minutos = Math.abs(Duration.between(t.getFecha(), actual.getFecha()).toMinutes());

                if (minutos <= 15) {
                    suma += t.getValor();
                    contador++;
                }
            }
        }

        if (contador >= 4 && suma >= 1000000) {
            registrarAlerta(actual, "Posible fragmentación de montos entre billeteras");
        }
    }

    /**
     * Detecta picos anómalos de actividad operativa en la cuenta si el número de transacciones
     * completadas en la última hora es igual o superior a 10.
     * @param usuario El usuario evaluado.
     * @param actual La transacción de referencia temporal.
     */
    private void detectarFrecuenciaInusual(Usuario usuario, Transaccion actual) {

        int transaccionesRecientes = 0;

        for (Transaccion t : usuario.getHistorialTransacciones()) {
            if (t == actual) continue;
            if (t.getEstado() != EstadoTransaccion.COMPLETADA) continue;

            long horas = Math.abs(Duration.between(t.getFecha(), actual.getFecha()).toHours());

            if (horas <= 1) transaccionesRecientes++;
        }

        if (transaccionesRecientes >= 10) {
            registrarAlerta(actual, "Frecuencia de transacciones inusualmente alta");
        }
    }

    /**
     * Clasifica como alerta preventiva cualquier movimiento financiero procesado en la
     * madrugada, específicamente entre la 1:00 AM y las 5:00 AM.
     * @param usuario El usuario firmante de la transacción.
     * @param actual La transacción con la estampa de tiempo a auditar.
     */
    private void detectarHorarioInusual(Usuario usuario, Transaccion actual) {

        int hora = actual.getFecha().getHour();

        if (hora >= 1 && hora <= 5) {
            registrarAlerta(actual, "Transacción realizada en horario inusual");
        }
    }

    /**
     * Calcula el promedio monetario aritmético de las transacciones históricas completadas con éxito
     * por el usuario.
     * @param usuario El usuario propietario de la cuenta.
     * @param actual La transacción en curso que se excluye del cálculo promedio.
     * @return El promedio double del valor de las operaciones, o 0 si no cuenta con movimientos previos.
     */
    private double calcularPromedioTransacciones(Usuario usuario, Transaccion actual) {

        double total = 0;
        int cantidad = 0;

        for (Transaccion t : usuario.getHistorialTransacciones()) {
            if (t == actual) continue;
            if (t.getEstado() != EstadoTransaccion.COMPLETADA) continue;

            total += t.getValor();
            cantidad++;
        }

        if (cantidad == 0) {
            return 0;
        }

        return total / cantidad;
    }

    /**
     * Registra una descripción de sospecha dentro de la transacción, asegurando que no se
     * ingresen descripciones duplicadas en la lista de alertas de fraude.
     * @param transaccion El objeto transacción destino de la alerta.
     * @param alerta La cadena descriptiva con el tipo de sospecha detectado.
     */
    private void registrarAlerta(Transaccion transaccion, String alerta) {
        if (!transaccion.getAlertasFraude().contains(alerta)) {
            transaccion.getAlertasFraude().add(alerta);
        }
    }

    /**
     * Actualiza de forma ponderada el nivel de riesgo global de la transacción basándose en
     * el volumen acumulado de alertas disparadas previamente.
     * @param transaccion La transacción cuyo indicador de riesgo será actualizado.
     */
    private void actualizarNivelRiesgo(Transaccion transaccion) {
        int cantidadAlertas = transaccion.getAlertasFraude().size();

        if (cantidadAlertas == 0) return;

        if (cantidadAlertas <= 2) {
            transaccion.setNivelRiesgo(NivelRiesgo.BAJO);
            return;
        }

        if (cantidadAlertas <= 4) {
            transaccion.setNivelRiesgo(NivelRiesgo.MEDIO);
            return;
        }

        transaccion.setNivelRiesgo(NivelRiesgo.ALTO);
    }

    /**
     * Filtra y recopila todas las transacciones del ecosistema financiero global que presenten
     * sospechas de fraude, omitiendo aquellas clasificadas como SIN_RIESGO.
     * @return Un ArrayList de transacciones que contienen algún grado de criticidad en su nivel de riesgo.
     */
    public java.util.ArrayList<Transaccion> obtenerTransaccionesRiesgosas() {
        java.util.ArrayList<Transaccion> riesgosas = new java.util.ArrayList<>();

        for (Transaccion t : sistemaService.obtenerTodasLasTransacciones()) {
            if (t.getNivelRiesgo() != NivelRiesgo.SIN_RIESGO) {
                riesgosas.add(t);
            }
        }

        return riesgosas;
    }
}