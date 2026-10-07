package ar.edu.unju.fi.arquitecturas.tp2.event;

/**
 * Evento de dominio emitido cuando un parámetro de configuración general
 * es modificado con éxito por un administrador.
 *
 * @param clave Identificador del parámetro modificado.
 * @param nuevoValor El valor actualizado.
 */
public record ConfiguracionActualizadaEvent(String clave, String nuevoValor) {
}
