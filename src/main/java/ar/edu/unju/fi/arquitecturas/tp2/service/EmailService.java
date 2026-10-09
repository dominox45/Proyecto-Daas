package ar.edu.unju.fi.arquitecturas.tp2.service;

/**
 * Contrato reusable para el envío de correos electrónicos.
 */
public interface EmailService {

    /**
     * Envía un correo electrónico cuyo contenido está expresado en HTML.
     *
     * @param destinatario dirección de correo del destinatario
     * @param asunto asunto del mensaje
     * @param contenidoHtml contenido HTML del mensaje
     */
    void enviarHtml(
            String destinatario,
            String asunto,
            String contenidoHtml
    );
}