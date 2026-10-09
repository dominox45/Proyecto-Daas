package ar.edu.unju.fi.arquitecturas.tp2.event;

import java.util.UUID;

/**
 * Evento publicado luego del alta de un nuevo cliente.
 *
 * @param clienteId identificador del cliente
 * @param nombre nombre del cliente
 * @param email correo electrónico del cliente
 * @param tokenActivacion token generado para confirmar el alta
 */
public record ClienteCreadoEvent(
        UUID clienteId,
        String nombre,
        String email,
        String tokenActivacion
) {
}