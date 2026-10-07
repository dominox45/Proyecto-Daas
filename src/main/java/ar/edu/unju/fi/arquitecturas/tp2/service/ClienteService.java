package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de servicios para la gestión de clientes del sistema bancario.
 *
 * <p>
 * Define las operaciones de negocio relacionadas con el registro,
 * búsqueda, persistencia y gestión del grupo familiar de clientes.
 * </p>
 *
 * <p>
 * Las operaciones utilizadas por la capa Controller trabajan con DTOs
 * para evitar exponer directamente las entidades de persistencia.
 * </p>
 *
 * @author MaxDz
 * @version 1.1.0
 * @see Cliente
 * @see ClienteRequestDto
 * @see ClienteResponseDto
 */
public interface ClienteService {

    /**
     * Registra un nuevo cliente en el sistema.
     *
     * <p>
     * La implementación debe validar las reglas de negocio correspondientes,
     * persistir el cliente y devolver únicamente los datos definidos
     * por el contrato de respuesta.
     * </p>
     *
     * @param request datos necesarios para registrar al cliente
     * @return DTO con los datos del cliente registrado
     * @throws IllegalArgumentException si los datos proporcionados
     *                                  incumplen una regla de negocio
     */
    ClienteResponseDto crear(ClienteRequestDto request);

    /**
     * Busca un cliente por su número de CUIL.
     *
     * @param cuil Clave Única de Identificación Laboral / Tributaria
     * @return {@link Optional} con el cliente si existe,
     *         o vacío en caso contrario
     */
    Optional<Cliente> buscarPorCuil(String cuil);

    /**
     * Asocia un cliente existente como adherente de otro cliente titular.
     *
     * <p>
     * El cliente indicado como titular debe ser un titular válido y el
     * adherente no puede ser el mismo cliente ni pertenecer a una relación
     * familiar incompatible.
     * </p>
     *
     * @param titularId identificador del cliente titular
     * @param adherenteId identificador del cliente que será asociado
     *                    como adherente
     * @throws RecursoNoEncontradoException si el titular o el adherente
     *                                      no existen
     * @throws IllegalArgumentException si la relación solicitada
     *                                  incumple una regla de negocio
     */
    void asociarAdherente(UUID titularId, UUID adherenteId);

    /**
     * Autoriza a un cliente adherente a realizar extracciones sobre
     * una cuenta específica perteneciente a su titular.
     *
     * <p>
     * La implementación deberá comprobar que el titular, el adherente
     * y la cuenta existan, que el cliente indicado sea realmente
     * adherente del titular y que la cuenta pertenezca al titular.
     * </p>
     *
     * <p>
     * La autorización no convierte al adherente en titular de la cuenta;
     * únicamente habilita su uso para las operaciones permitidas
     * al adherente.
     * </p>
     *
     * @param titularId identificador del cliente titular
     * @param adherenteId identificador del cliente adherente
     * @param cuentaId identificador de la cuenta que se desea autorizar
     * @throws RecursoNoEncontradoException si alguno de los recursos
     *                                      indicados no existe
     * @throws IllegalArgumentException si la relación entre titular,
     *                                  adherente y cuenta es inválida
     */
    void autorizarCuentaAdherente(
            UUID titularId,
            UUID adherenteId,
            UUID cuentaId
    );

    /**
     * Persiste un cliente en la base de datos.
     *
     * <p>
     * Este método se conserva como parte del contrato desarrollado
     * en etapas anteriores del proyecto.
     * </p>
     *
     * @param cliente entidad que se desea persistir
     * @return cliente persistido
     */
    Cliente guardar(Cliente cliente);
}