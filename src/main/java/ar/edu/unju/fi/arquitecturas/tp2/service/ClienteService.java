package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;

import java.util.Optional;

/**
 * Contrato de servicios para la gestión de clientes del sistema bancario.
 *
 * <p>
 * Define las operaciones de negocio relacionadas con el registro,
 * búsqueda y persistencia de clientes.
 * </p>
 *
 * <p>
 * Las operaciones utilizadas por la capa Controller trabajan con DTOs
 * para evitar exponer directamente las entidades de persistencia.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
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