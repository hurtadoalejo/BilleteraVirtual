package com.proyectofinal.billeteravirtual.config;

import com.proyectofinal.billeteravirtual.enums.TipoBilletera;
import com.proyectofinal.billeteravirtual.model.*;
import com.proyectofinal.billeteravirtual.service.BilleteraService;
import com.proyectofinal.billeteravirtual.service.TransaccionService;
import com.proyectofinal.billeteravirtual.service.UsuarioService;
import com.proyectofinal.billeteravirtual.util.ResultadoTransaccion;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Random;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UsuarioService usuarioService,
            BilleteraService billeteraService,
            TransaccionService transaccionService
    ) {

        return args -> {

            Usuario u1 = crearUsuario("Alejandro Hurtado", "1092850037", "alejohg2911@gmail.com", "3161111111", "123");
            Usuario u2 = crearUsuario("Veronica Ibarra", "1036448546", "alejandro.hurtadog@uqvirtual.edu.co", "3161111111", "123");
            Usuario u3 = crearUsuario("Carlos Ramirez", "1000000001", "carlos@gmail.com", "3163333333", "123");
            Usuario u4 = crearUsuario("Laura Martinez", "1000000002", "laura@gmail.com", "3164444444", "123");
            Usuario u5 = crearUsuario("Andres Gomez", "1000000003", "andres@gmail.com", "3165555555", "123");
            Usuario u6 = crearUsuario("Sofia Torres", "1000000004", "sofia@gmail.com", "3166666666", "123");

            usuarioService.registrarUsuario(u1);
            usuarioService.registrarUsuario(u2);
            usuarioService.registrarUsuario(u3);
            usuarioService.registrarUsuario(u4);
            usuarioService.registrarUsuario(u5);
            usuarioService.registrarUsuario(u6);

            crearBilleteras(usuarioService, billeteraService, "1092850037");
            crearBilleteras(usuarioService, billeteraService, "1036448546");
            crearBilleteras(usuarioService, billeteraService, "1000000001");
            crearBilleteras(usuarioService, billeteraService, "1000000002");
            crearBilleteras(usuarioService, billeteraService, "1000000003");
            crearBilleteras(usuarioService, billeteraService, "1000000004");

            String[] cedulas = {
                    "1092850037",
                    "1036448546",
                    "1000000001",
                    "1000000002",
                    "1000000003",
                    "1000000004"
            };

            Random random = new Random();

            for (String cedula : cedulas) {

                Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

                for (String idBilletera : usuario.getBilleteras().keySet()) {

                    double saldoInicial = 800000 + random.nextInt(1200000);

                    Transaccion t = transaccionService
                            .recargarSinCorreo(cedula, idBilletera, saldoInicial)
                            .getTransaccion();

                    t.setFecha(fechaRandom(random));
                }
            }

            for (int i = 0; i < 100; i++) {

                int tipo = random.nextInt(3);

                if (tipo == 0) {

                    String cedula = cedulas[random.nextInt(cedulas.length)];

                    Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

                    java.util.ArrayList<String> ids =
                            new java.util.ArrayList<>(usuario.getBilleteras().keySet());

                    String billetera = ids.get(random.nextInt(ids.size()));

                    double valor = 20000 + random.nextInt(400000);

                    ResultadoTransaccion resultado =
                            transaccionService.recargarSinCorreo(cedula, billetera, valor);

                    if (resultado.isOk()) {
                        resultado.getTransaccion().setFecha(fechaRandom(random));
                    }

                } else if (tipo == 1) {

                    String cedula = cedulas[random.nextInt(cedulas.length)];

                    Usuario usuario = usuarioService.buscarUsuarioPorCedula(cedula);

                    java.util.ArrayList<String> ids =
                            new java.util.ArrayList<>(usuario.getBilleteras().keySet());

                    String billetera = ids.get(random.nextInt(ids.size()));

                    Billetera b = usuario.getBilleteras().get(billetera);

                    if (b.getSaldo() > 50000) {

                        double maximo = Math.min(b.getSaldo() * 0.6, 300000);
                        double valor = 10000 + random.nextDouble() * maximo;

                        ResultadoTransaccion resultado =
                                transaccionService.retirarSinCorreo(cedula, billetera, valor);

                        if (resultado.isOk()) {
                            resultado.getTransaccion().setFecha(fechaRandom(random));
                        }
                    }

                } else {

                    String cedulaOrigen = cedulas[random.nextInt(cedulas.length)];
                    String cedulaDestino = cedulas[random.nextInt(cedulas.length)];

                    Usuario usuarioOrigen =
                            usuarioService.buscarUsuarioPorCedula(cedulaOrigen);

                    Usuario usuarioDestino =
                            usuarioService.buscarUsuarioPorCedula(cedulaDestino);

                    java.util.ArrayList<String> billeterasOrigen =
                            new java.util.ArrayList<>(usuarioOrigen.getBilleteras().keySet());

                    java.util.ArrayList<String> billeterasDestino =
                            new java.util.ArrayList<>(usuarioDestino.getBilleteras().keySet());

                    String idOrigen =
                            billeterasOrigen.get(random.nextInt(billeterasOrigen.size()));

                    String idDestino =
                            billeterasDestino.get(random.nextInt(billeterasDestino.size()));

                    Billetera origen =
                            usuarioOrigen.getBilleteras().get(idOrigen);

                    if (origen.getSaldo() > 50000) {

                        double maximo = Math.min(origen.getSaldo() * 0.4, 250000);
                        double valor = 10000 + random.nextDouble() * maximo;

                        ResultadoTransaccion resultado =
                                transaccionService.transferirSinCorreo(
                                        cedulaOrigen,
                                        idOrigen,
                                        idDestino,
                                        valor,
                                        null
                                );

                        if (resultado.isOk()) {
                            resultado.getTransaccion().setFecha(fechaRandom(random));
                        }
                    }
                }
            }

            System.out.println("Datos inicializados correctamente.");
        };
    }

    private Usuario crearUsuario(
            String nombre,
            String cedula,
            String correo,
            String telefono,
            String password
    ) {

        Usuario usuario = new Usuario();

        usuario.setNombreCompleto(nombre);
        usuario.setCedula(cedula);
        usuario.setCorreoElectronico(correo);
        usuario.setNumeroTelefonico(telefono);
        usuario.setPassword(password);

        return usuario;
    }

    private void crearBilleteras(
            UsuarioService usuarioService,
            BilleteraService billeteraService,
            String cedula
    ) {

        Billetera b1 = new Billetera();
        b1.setNombre("Principal");
        b1.setTipo(TipoBilletera.AHORRO);

        Billetera b2 = new Billetera();
        b2.setNombre("Secundaria");
        b2.setTipo(TipoBilletera.GASTOS_DIARIOS);

        billeteraService.agregarBilletera(cedula, b1);
        billeteraService.agregarBilletera(cedula, b2);
    }

    private LocalDateTime fechaRandom(Random random) {

        int mes = 1 + random.nextInt(5);

        int diaMaximo = switch (mes) {
            case 2 -> 28;
            case 4 -> 30;
            default -> 31;
        };

        int dia = 1 + random.nextInt(diaMaximo);

        int hora = random.nextInt(24);
        int minuto = random.nextInt(60);

        return LocalDateTime.of(2026, mes, dia, hora, minuto);
    }
}