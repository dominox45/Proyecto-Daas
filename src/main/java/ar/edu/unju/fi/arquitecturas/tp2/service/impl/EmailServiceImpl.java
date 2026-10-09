package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Implementación del servicio reusable de correo electrónico
 * utilizando la infraestructura proporcionada por Spring Mail.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final String remitente;

    /**
     * Construye el servicio de correo.
     *
     * @param mailSender infraestructura de envío configurada por Spring
     * @param remitente dirección utilizada como remitente
     */
    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${app.email.remitente:}") String remitente) {

        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    /**
     * Envía un mensaje HTML.
     *
     * @param destinatario correo del destinatario
     * @param asunto asunto del mensaje
     * @param contenidoHtml contenido HTML
     */
    @Override
    public void enviarHtml(
            String destinatario,
            String asunto,
            String contenidoHtml) {

        MimeMessage mensaje =
                mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mensaje,
                            false,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            if (remitente != null
                    && !remitente.isBlank()) {
                helper.setFrom(remitente);
            }

        } catch (MessagingException ex) {
            throw new IllegalStateException(
                    "No se pudo preparar el correo electrónico",
                    ex
            );
        }

        mailSender.send(mensaje);
    }
}