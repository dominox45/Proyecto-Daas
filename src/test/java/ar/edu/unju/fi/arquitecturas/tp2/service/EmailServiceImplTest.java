package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio reusable de email HTML.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    /**
     * Verifica que el servicio construya y envíe
     * correctamente un mensaje HTML.
     *
     * @throws Exception si el mensaje no puede inspeccionarse
     */
    @Test
    void deberiaEnviarCorreoHtml()
            throws Exception {

        MimeMessage mensaje =
                new MimeMessage(
                        Session.getInstance(
                                new Properties()
                        )
                );

        when(mailSender.createMimeMessage())
                .thenReturn(mensaje);

        EmailServiceImpl emailService =
                new EmailServiceImpl(
                        mailSender,
                        "no-reply@test.com"
                );

        emailService.enviarHtml(
                "cliente@test.com",
                "Activación de cuenta",
                "<h1>Bienvenido</h1>"
        );

        verify(mailSender)
                .send(mensaje);

        mensaje.saveChanges();

        assertEquals(
                "Activación de cuenta",
                mensaje.getSubject()
        );

        assertEquals(
                "cliente@test.com",
                mensaje.getAllRecipients()[0]
                        .toString()
        );

        assertEquals(
                "no-reply@test.com",
                mensaje.getFrom()[0]
                        .toString()
        );

        assertTrue(
                mensaje.getContentType()
                        .toLowerCase()
                        .contains("text/html")
        );
    }
}