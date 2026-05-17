package com.proyectofinal.billeteravirtual.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envía de forma asíncrona un correo electrónico en formato HTML o texto plano.
     * @param destino La dirección de correo electrónico del destinatario.
     * @param asunto El tema o título del correo electrónico.
     * @param mensaje El contenido del mensaje (soporta etiquetas HTML).
     */
    @Async
    public void enviarCorreo(String destino, String asunto, String mensaje) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }
    }
}