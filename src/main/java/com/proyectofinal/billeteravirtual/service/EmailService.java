package com.proyectofinal.billeteravirtual.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreo(String destino, String asunto, String mensaje) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(destino);
        mail.setSubject(asunto);
        mail.setText(mensaje);

        mailSender.send(mail);
    }
}