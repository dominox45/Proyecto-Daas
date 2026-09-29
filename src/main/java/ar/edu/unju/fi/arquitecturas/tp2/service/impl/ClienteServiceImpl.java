package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.ClienteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementación del contrato {@link ClienteService} para la gestión
 * de clientes del sistema bancario.
 *
 * <p>
 * Centraliza la lógica de negocio asociada al registro de clientes,
 * incluyendo las validaciones de unicidad, la conversión entre DTOs
 * y entidades, la persistencia mediante el repositorio y el registro
 * de eventos utilizando logs.
 * </p>
 *
 * <p>
 * De esta manera, la capa Controller puede limitarse a recibir
 * solicitudes HTTP y delegar el procesamiento a la capa de servicios.
 * </p>
 *
 * @author MaxDz
 * @version 1.0.0
 * @see ClienteService
 * @see ClienteRepository
 * @see ClienteRequestDto
 * @see ClienteResponseDto
 */
@Slf4j
@Service
public class ClienteServiceImpl implements ClienteService {

    /**
     * Repositorio utilizado para acceder a la persistencia de clientes.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Construye el servicio utilizando inyección de dependencias
     * mediante constructor.
     *
     * @param clienteRepository repositorio de clientes
     */
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Registra un nuevo cliente en el sistema.
     *
     * <p>
     * Antes de persistir la entidad se verifican las reglas de unicidad
     * definidas para CUIL y correo electrónico. Posteriormente se realiza
     * el mapeo desde {@link ClienteRequestDto} hacia {@link Cliente},
     * se persiste la entidad y se construye el DTO de respuesta.
     * </p>
     *
     * @param request datos necesarios para registrar al cliente
     * @return DTO con los datos del cliente registrado
     * @throws IllegalArgumentException si la solicitud es nula o existe
     *                                  otro cliente con el mismo CUIL
     *                                  o correo electrónico
     */
    @Override
    public ClienteResponseDto crear(ClienteRequestDto request) {
        validarDuplicados(request);

        Cliente cliente = mapearAEntidad(request);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        log.info(
                "Cliente creado correctamente. id={}, cuil={}",
                clienteGuardado.getId(),
                clienteGuardado.getCuil()
        );

        return mapearAResponse(clienteGuardado);
    }

    /**
     * Busca un cliente registrado mediante su número de CUIL.
     *
     * @param cuil Clave Única de Identificación Laboral / Tributaria
     * @return {@link Optional} con el cliente encontrado,
     *         o vacío si no existe
     */
    @Override
    public Optional<Cliente> buscarPorCuil(String cuil) {
        return clienteRepository.findByCuil(cuil);
    }

    /**
     * Persiste una entidad {@link Cliente}.
     *
     * <p>
     * Este método forma parte del contrato implementado en etapas
     * anteriores del proyecto y se conserva para mantener
     * compatibilidad con las operaciones existentes.
     * </p>
     *
     * @param cliente entidad que se desea persistir
     * @return cliente persistido
     */
    @Override
    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    /**
     * Verifica que no exista otro cliente registrado con el mismo
     * CUIL o correo electrónico.
     *
     * <p>
     * El correo electrónico solamente se valida cuando fue informado
     * en la solicitud, ya que actualmente es un dato opcional dentro
     * de {@link ClienteRequestDto}.
     * </p>
     *
     * @param request datos del cliente que se desea registrar
     * @throws IllegalArgumentException si la solicitud es nula,
     *                                  si el CUIL ya está registrado
     *                                  o si el email ya está registrado
     */
    private void validarDuplicados(ClienteRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos del cliente son obligatorios"
            );
        }

        if (clienteRepository.findByCuil(request.getCuil()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el CUIL indicado"
            );
        }

        if (request.getEmail() != null
                && !request.getEmail().isBlank()
                && clienteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el email indicado"
            );
        }
    }

    /**
     * Convierte los datos recibidos desde la API en una entidad
     * {@link Cliente} apta para ser persistida.
     *
     * @param request DTO con los datos de entrada
     * @return nueva entidad cliente construida a partir de la solicitud
     */
    private Cliente mapearAEntidad(ClienteRequestDto request) {
        return Cliente.builder()
                .nombre(request.getNombre())
                .cuil(request.getCuil())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .build();
    }

    /**
     * Convierte una entidad {@link Cliente} en el DTO que será
     * expuesto como respuesta de la API.
     *
     * <p>
     * La conversión evita devolver directamente la entidad JPA
     * desde la capa Controller.
     * </p>
     *
     * @param cliente entidad que se desea transformar
     * @return DTO con los datos públicos del cliente
     */
    private ClienteResponseDto mapearAResponse(Cliente cliente) {
        return new ClienteResponseDto(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getCuil(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion()
        );
    }
}