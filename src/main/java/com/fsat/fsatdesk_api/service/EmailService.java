package com.fsat.fsatdesk_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String fromEmail;

    /**
     * Envía un correo HTML de forma asíncrona (no bloquea el hilo principal).
     * @param to      Destinatario (correo del técnico)
     * @param subject Asunto del correo
     * @param body    Cuerpo del mensaje (HTML)
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML
            mailSender.send(message);
            log.info("Correo enviado a: {}", to);
        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", to, e.getMessage(), e); // <-- Añadir la excepción completa
            
        }
    }

    /**
     * Envía un correo HTML con copia (CC) de forma asíncrona.
     * @param to      Destinatario principal
     * @param cc      Destinatario en copia
     * @param subject Asunto del correo
     * @param body    Cuerpo del mensaje (HTML)
     */

    @Async
    public void sendHtmlEmailWithCC(String to, String cc, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setCc(cc);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.info("Correo enviado a: {} (CC: {})", to, cc);
        } catch (MessagingException e) {
            log.error("Error al enviar correo: {}", e.getMessage());
        }
    }

    

}