package ar.edu.unju.fi.arquitecturas.tp2.listener;

import ar.edu.unju.fi.arquitecturas.tp2.event.ClienteCreadoEvent;
import ar.edu.unju.fi.arquitecturas.tp2.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.HtmlUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Pruebas unitarias del listener encargado de procesar
 * la notificación de alta de clientes.
 */
@ExtendWith(MockitoExtension.class)
class ClienteCreadoEventListenerTest {

    @Mock
    private EmailService emailService;

    /**
     * Verifica que el evento genere un email HTML
     * con el enlace de activación correspondiente.
     */
    @Test
    void deberiaEnviarEmailDeActivacion() {

        String token =
                UUID.randomUUID().toString();

        ClienteCreadoEvent evento =
                new ClienteCreadoEvent(
                        UUID.randomUUID(),
                        "Ana López",
                        "ana.lopez@email.com",
                        token
                );

        ClienteCreadoEventListener listener =
                new ClienteCreadoEventListener(
                        emailService,
                        "http://localhost:8080/api/v1/clientes/activar"
                );

        listener.procesarAltaCliente(evento);

        ArgumentCaptor<String> htmlCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailService).enviarHtml(
                eq("ana.lopez@email.com"),
                eq("Activación de cuenta"),
                htmlCaptor.capture()
        );

        String html =
                htmlCaptor.getValue();

        assertTrue(
                html.contains(
                        HtmlUtils.htmlEscape("Ana López")
                )
        );

        assertTrue(
                html.contains(
                        "/api/v1/clientes/activar?token="
                                + token
                )
        );

        assertTrue(
                html.contains("24 horas")
        );

        assertTrue(
                html.contains("Activar cuenta")
        );
    }
}