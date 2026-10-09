package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.ClienteResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.ClienteService;
import ar.edu.unju.fi.arquitecturas.tp2.event.ClienteCreadoEvent;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCliente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Implementación del contrato {@link ClienteService} para la gestión
 * de clientes del sistema bancario.
 *
 * <p>
 * Centraliza la lógica de negocio asociada al registro de clientes,
 * incluyendo las validaciones de unicidad, la conversión entre DTOs
 * y entidades, la persistencia y la gestión de relaciones de grupo familiar.
 * </p>
 *
 * <p>
 * También gestiona las autorizaciones explícitas que permiten a un
 * cliente adherente realizar extracciones sobre determinadas cuentas
 * pertenecientes a su titular.
 * </p>
 *
 * <p>
 * De esta manera, la capa Controller puede limitarse a recibir
 * solicitudes HTTP y delegar el procesamiento a la capa de servicios.
 * </p>
 *
 * @author MaxDz
 * @version 1.2.0
 * @see ClienteService
 * @see ClienteRepository
 * @see CuentaFinancieraRepository
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
     * Repositorio utilizado para recuperar las cuentas que serán
     * autorizadas a los clientes adherentes.
     */
    private final CuentaFinancieraRepository cuentaRepository;

    /**
     * Publicador utilizado para emitir eventos de dominio
     * relacionados con el ciclo de vida del cliente.
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Construye el servicio utilizando inyección de dependencias
     * mediante constructor.
     *
     * @param clienteRepository repositorio de clientes
     * @param cuentaRepository repositorio de cuentas financieras
     */
    public ClienteServiceImpl(
            ClienteRepository clienteRepository,
            CuentaFinancieraRepository cuentaRepository,
            ApplicationEventPublisher eventPublisher) {

        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.eventPublisher = eventPublisher;
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
    @Transactional
    public ClienteResponseDto crear(
            ClienteRequestDto request) {

        validarDuplicados(request);

        Cliente cliente = mapearAEntidad(request);

        LocalDateTime fechaAlta =
                LocalDateTime.now();

        cliente.setEstado(
                EstadoCliente.PENDIENTE_ACTIVACION
        );

        cliente.setTokenActivacion(
                UUID.randomUUID().toString()
        );

        cliente.setTokenActivacionExpiraEn(
                fechaAlta.plusHours(24)
        );

        Cliente clienteGuardado =
                clienteRepository.save(cliente);

        eventPublisher.publishEvent(
                new ClienteCreadoEvent(
                        clienteGuardado.getId(),
                        clienteGuardado.getNombre(),
                        clienteGuardado.getEmail(),
                        clienteGuardado.getTokenActivacion()
                )
        );

        log.info(
                "Cliente creado correctamente. id={}, cuil={}, estado={}",
                clienteGuardado.getId(),
                clienteGuardado.getCuil(),
                clienteGuardado.getEstado()
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
     * Asocia un cliente existente como adherente de otro cliente titular.
     *
     * <p>
     * La relación de grupo familiar se representa mediante el atributo
     * {@link Cliente#getTitular()} del cliente adherente. Un titular válido
     * no debe poseer a su vez otro titular.
     * </p>
     *
     * @param titularId identificador del cliente titular
     * @param adherenteId identificador del cliente que será adherente
     * @throws IllegalArgumentException si alguno de los identificadores
     *                                  es nulo, ambos corresponden al mismo
     *                                  cliente o la relación solicitada es inválida
     * @throws RecursoNoEncontradoException si alguno de los clientes no existe
     */
    @Override
    @Transactional
    public void asociarAdherente(
            UUID titularId,
            UUID adherenteId) {

        if (titularId == null) {
            throw new IllegalArgumentException(
                    "El identificador del titular es obligatorio"
            );
        }

        if (adherenteId == null) {
            throw new IllegalArgumentException(
                    "El identificador del adherente es obligatorio"
            );
        }

        if (titularId.equals(adherenteId)) {
            throw new IllegalArgumentException(
                    "Un cliente no puede ser adherente de sí mismo"
            );
        }

        Cliente titular = clienteRepository.findById(titularId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El cliente titular no existe"
                        )
                );

        Cliente adherente = clienteRepository.findById(adherenteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El cliente adherente no existe"
                        )
                );

        if (titular.getTitular() != null) {
            throw new IllegalArgumentException(
                    "Un cliente adherente no puede actuar como titular"
            );
        }

        if (adherente.getTitular() != null) {
            if (titular.getId().equals(
                    adherente.getTitular().getId()
            )) {
                throw new IllegalArgumentException(
                        "El cliente ya es adherente del titular indicado"
                );
            }

            throw new IllegalArgumentException(
                    "El cliente ya posee un titular asociado"
            );
        }

        adherente.setTitular(titular);
        clienteRepository.save(adherente);

        log.info(
                "Adherente asociado correctamente. titularId={}, adherenteId={}",
                titularId,
                adherenteId
        );
    }

    /**
     * Autoriza a un cliente adherente a realizar extracciones
     * sobre una cuenta específica perteneciente a su titular.
     *
     * <p>
     * La autorización es independiente de la titularidad de la cuenta.
     * El adherente conserva su relación con el titular y únicamente
     * incorpora la cuenta dentro de su conjunto de cuentas autorizadas.
     * </p>
     *
     * @param titularId identificador del cliente titular
     * @param adherenteId identificador del cliente adherente
     * @param cuentaId identificador de la cuenta que se desea autorizar
     * @throws IllegalArgumentException si algún identificador es nulo,
     *                                  la relación familiar no corresponde,
     *                                  la cuenta no pertenece al titular
     *                                  o la autorización ya existe
     * @throws RecursoNoEncontradoException si alguno de los recursos
     *                                      indicados no existe
     */
    @Override
    @Transactional
    public void autorizarCuentaAdherente(
            UUID titularId,
            UUID adherenteId,
            UUID cuentaId) {

        if (titularId == null) {
            throw new IllegalArgumentException(
                    "El identificador del titular es obligatorio"
            );
        }

        if (adherenteId == null) {
            throw new IllegalArgumentException(
                    "El identificador del adherente es obligatorio"
            );
        }

        if (cuentaId == null) {
            throw new IllegalArgumentException(
                    "El identificador de la cuenta es obligatorio"
            );
        }

        Cliente titular = clienteRepository.findById(titularId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El cliente titular no existe"
                        )
                );

        Cliente adherente = clienteRepository.findById(adherenteId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El cliente adherente no existe"
                        )
                );

        CuentaFinanciera cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La cuenta no existe"
                        )
                );

        /*
         * El cliente debe pertenecer exactamente al grupo familiar
         * del titular indicado.
         */
        if (adherente.getTitular() == null
                || adherente.getTitular().getId() == null
                || !titularId.equals(
                adherente.getTitular().getId()
        )) {

            throw new IllegalArgumentException(
                    "El cliente no es adherente del titular indicado"
            );
        }

        /*
         * La cuenta que se desea autorizar debe pertenecer realmente
         * al titular del grupo familiar.
         */
        boolean cuentaPerteneceAlTitular =
                cuenta.getTitulares()
                        .stream()
                        .anyMatch(cliente ->
                                titularId.equals(cliente.getId())
                        );

        if (!cuentaPerteneceAlTitular) {
            throw new IllegalArgumentException(
                    "La cuenta no pertenece al titular indicado"
            );
        }

        /*
         * No se permite registrar dos veces la misma autorización.
         */
        boolean yaAutorizada =
                adherente.getCuentasAutorizadas()
                        .stream()
                        .anyMatch(cuentaAutorizada ->
                                cuentaId.equals(
                                        cuentaAutorizada.getId()
                                )
                        );

        if (yaAutorizada) {
            throw new IllegalArgumentException(
                    "La cuenta ya está autorizada para el adherente"
            );
        }

        adherente.autorizarCuenta(cuenta);
        clienteRepository.save(adherente);

        log.info(
                "Cuenta autorizada para adherente. titularId={}, adherenteId={}, cuentaId={}",
                titular.getId(),
                adherente.getId(),
                cuenta.getId()
        );
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
    /**
     * Activa un cliente cuando el token informado existe y continúa vigente.
     *
     * @param token token recibido desde el enlace de activación
     * @throws IllegalArgumentException si el token es vacío,
     *                                  inexistente o está vencido
     */
    @Override
    @Transactional
    public void activar(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "El token de activación es obligatorio"
            );
        }

        Cliente cliente =
                clienteRepository
                        .findByTokenActivacion(token)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El token de activación no existe"
                                )
                        );

        LocalDateTime ahora =
                LocalDateTime.now();

        if (cliente.getTokenActivacionExpiraEn() == null
                || !cliente.getTokenActivacionExpiraEn()
                .isAfter(ahora)) {

            throw new IllegalArgumentException(
                    "El token de activación está vencido"
            );
        }

        cliente.setEstado(
                EstadoCliente.ACTIVO
        );

        cliente.setFechaActivacion(ahora);

        /*
         * El token deja de ser reutilizable una vez completada
         * correctamente la activación.
         */
        cliente.setTokenActivacion(null);
        cliente.setTokenActivacionExpiraEn(null);

        clienteRepository.save(cliente);

        log.info(
                "Cliente activado correctamente. id={}",
                cliente.getId()
        );
    }
}