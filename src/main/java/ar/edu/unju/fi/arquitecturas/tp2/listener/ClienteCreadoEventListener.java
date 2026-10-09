package ar.edu.unju.fi.arquitecturas.tp2.listener;

import ar.edu.unju.fi.arquitecturas.tp2.event.ClienteCreadoEvent;
import ar.edu.unju.fi.arquitecturas.tp2.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Procesa de manera asíncrona el evento generado después del alta
 * de un cliente y envía el correo de activación.
 */
@Slf4j
@Component
public class ClienteCreadoEventListener {

    private final EmailService emailService;
    private final String baseUrlActivacion;

    public ClienteCreadoEventListener(
            EmailService emailService,
            @Value("${app.activacion.base-url}") String baseUrlActivacion) {

        this.emailService = emailService;
        this.baseUrlActivacion = baseUrlActivacion;
    }

    /**
     * Envía el mensaje de bienvenida una vez confirmada
     * la transacción de creación del cliente.
     *
     * @param evento datos necesarios para construir la notificación
     */
    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void procesarAltaCliente(
            ClienteCreadoEvent evento) {

        String tokenCodificado =
                URLEncoder.encode(
                        evento.tokenActivacion(),
                        StandardCharsets.UTF_8
                );

        String enlaceActivacion =
                baseUrlActivacion
                        + "?token="
                        + tokenCodificado;

        String nombreSeguro =
                HtmlUtils.htmlEscape(evento.nombre());

        String enlaceSeguro =
                HtmlUtils.htmlEscape(enlaceActivacion);

        String contenidoHtml = """
                <!DOCTYPE html>
                <html lang="es">
                <body>
                    <h2>Bienvenido/a, %s</h2>
                    <p>
                        Tu cuenta fue registrada correctamente.
                        Para completar la activación hacé clic en el siguiente botón:
                    </p>

                    <p>
                        <a href="%s"
                           style="display:inline-block;
                                  padding:12px 18px;
                                  text-decoration:none;
                                  border:1px solid #333;">
                            Activar cuenta
                        </a>
                    </p>

                    <p>
                        Este enlace tiene una validez de 24 horas.
                    </p>

                    <p>
                        Si el botón no funciona, podés utilizar este enlace:
                    </p>

                    <p>%s</p>
                </body>
                </html>
                """.formatted(
                nombreSeguro,
                enlaceSeguro,
                enlaceSeguro
        );

        try {
            emailService.enviarHtml(
                    evento.email(),
                    "Activación de cuenta",
                    contenidoHtml
            );

            log.info(
                    "Email de activación enviado. clienteId={}",
                    evento.clienteId()
            );

        } catch (RuntimeException ex) {
            /*
             * La notificación es asíncrona y no debe revertir
             * el alta ya confirmada del cliente.
             */
            log.error(
                    "No se pudo enviar el email de activación. clienteId={}",
                    evento.clienteId(),
                    ex
            );
        }
    }
}