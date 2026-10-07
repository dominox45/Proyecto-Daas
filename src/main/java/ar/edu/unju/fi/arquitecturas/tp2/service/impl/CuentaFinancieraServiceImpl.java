package ar.edu.unju.fi.arquitecturas.tp2.service.impl;

import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.dto.CuentaResponseDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.exception.SaldoInsuficienteException;
import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.CuentaFinancieraService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementación del contrato {@link CuentaFinancieraService} para la gestión
 * de cuentas financieras del sistema bancario.
 *
 * <p>
 * Centraliza la lógica de negocio correspondiente a la creación y consulta
 * de cuentas, así como las operaciones de depósito y extracción de fondos.
 * </p>
 *
 * <p>
 * La implementación también se encarga de determinar el subtipo concreto
 * de cuenta solicitado, validar sus datos específicos, mantener la relación
 * entre el cliente titular y la cuenta, y convertir las entidades en DTOs
 * antes de devolver información a la capa Controller.
 * </p>
 *
 * @author MaxDz
 * @version 1.1.0
 * @see CuentaFinancieraService
 * @see CuentaFinancieraRepository
 * @see ClienteRepository
 * @see CuentaRequestDto
 * @see CuentaResponseDto
 */
@Slf4j
@Service
public class CuentaFinancieraServiceImpl implements CuentaFinancieraService {

    /**
     * Repositorio utilizado para acceder a la persistencia
     * de cuentas financieras.
     */
    private final CuentaFinancieraRepository cuentaRepository;

    /**
     * Repositorio utilizado para registrar las transacciones
     * generadas por depósitos y extracciones.
     */
    private final TransaccionRepository transaccionRepository;

    /**
     * Repositorio utilizado para recuperar al cliente titular
     * y mantener la asociación con sus cuentas.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Construye el servicio mediante inyección de dependencias por constructor.
     *
     * @param cuentaRepository      repositorio de cuentas financieras
     * @param transaccionRepository repositorio de transacciones
     * @param clienteRepository     repositorio de clientes
     */
    public CuentaFinancieraServiceImpl(
            CuentaFinancieraRepository cuentaRepository,
            TransaccionRepository transaccionRepository,
            ClienteRepository clienteRepository) {

        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Registra una nueva cuenta financiera y la asocia con su cliente titular.
     *
     * <p>
     * Antes de crear la cuenta se comprueba la existencia del cliente,
     * la unicidad del CBU y del alias y la presencia de los datos
     * obligatorios correspondientes al subtipo solicitado.
     * </p>
     *
     * <p>
     * Las cuentas nuevas se inicializan con saldo operativo igual a cero
     * y estado {@link EstadoCuenta#ACTIVA}.
     * </p>
     *
     * @param request datos necesarios para crear la cuenta
     * @return DTO con los datos de la cuenta registrada
     * @throws IllegalArgumentException     si la solicitud es nula, existe
     *                                      otra cuenta con el mismo CBU o alias,
     *                                      o faltan datos específicos del subtipo
     * @throws RecursoNoEncontradoException si el cliente indicado no existe
     */
    @Override
    @Transactional
    public CuentaResponseDto crear(CuentaRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de la cuenta son obligatorios"
            );
        }

        if (request.getClienteId() == null) {
            throw new IllegalArgumentException(
                    "El cliente es obligatorio"
            );
        }

        if (request.getTipoCuenta() == null) {
            throw new IllegalArgumentException(
                    "El tipo de cuenta es obligatorio"
            );
        }

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El cliente no existe"
                        )
                );
        /*
         * Un cliente adherente no puede actuar como titular
         * de una nueva cuenta financiera.
         */
        if (cliente.getTitular() != null) {
            throw new IllegalStateException(
                    "Los clientes adherentes solo pueden realizar extracciones"
            );
        }

        validarDuplicados(request);

        CuentaFinanciera cuenta = crearCuentaSegunTipo(request);

        /*
         * Decisión de diseño del proyecto:
         * toda cuenta nueva comienza sin saldo y en estado activo.
         */
        cuenta.setCbu(request.getCbu());
        cuenta.setAlias(request.getAlias());
        cuenta.setSaldoOperativo(BigDecimal.ZERO);
        cuenta.setEstado(EstadoCuenta.ACTIVA);

        CuentaFinanciera cuentaGuardada =
                cuentaRepository.save(cuenta);

        /*
         * Cliente es el lado propietario de la relación ManyToMany,
         * por lo que la asociación se mantiene desde esta entidad.
         */
        cliente.agregarCuenta(cuentaGuardada);
        clienteRepository.save(cliente);

        log.info(
                "Cuenta creada correctamente. id={}, tipo={}, cbu={}, clienteId={}",
                cuentaGuardada.getId(),
                request.getTipoCuenta(),
                cuentaGuardada.getCbu(),
                cliente.getId()
        );

        return mapearAResponse(cuentaGuardada);
    }

    /**
     * Busca una cuenta financiera mediante su CBU.
     *
     * @param cbu Clave Bancaria Uniforme de la cuenta
     * @return DTO con los datos de la cuenta encontrada
     * @throws RecursoNoEncontradoException si no existe una cuenta
     *                                      con el CBU indicado
     */
    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDto buscarPorCbu(String cbu) {
        CuentaFinanciera cuenta = cuentaRepository.findByCbu(cbu)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La cuenta no existe"
                        )
                );

        return mapearAResponse(cuenta);
    }

    /**
     * Realiza un depósito sobre una cuenta financiera activa.
     *
     * <p>
     * Además de actualizar el saldo operativo, registra una transacción
     * de tipo {@link TipoTransaccion#DEPOSITO} con estado completado.
     * </p>
     *
     * @param cuentaId identificador de la cuenta
     * @param monto    monto que se desea depositar
     * @return cuenta financiera actualizada
     * @throws IllegalArgumentException     si el monto es nulo o no es positivo
     * @throws RecursoNoEncontradoException si la cuenta no existe
     * @throws IllegalStateException        si la cuenta no se encuentra activa
     */
    @Override
    @Transactional
    public CuentaFinanciera depositar(UUID cuentaId, BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser positivo"
            );
        }

        CuentaFinanciera cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La cuenta no existe"
                        )
                );

        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException(
                    "La cuenta no está activa"
            );
        }

        cuenta.setSaldoOperativo(
                cuenta.getSaldoOperativo().add(monto)
        );

        CuentaFinanciera cuentaGuardada =
                cuentaRepository.save(cuenta);

        Transaccion transaccion = Transaccion.builder()
                .fechaHora(LocalDateTime.now())
                .monto(monto)
                .tipo(TipoTransaccion.DEPOSITO)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(cuentaGuardada)
                .build();

        transaccionRepository.save(transaccion);

        log.info(
                "Depósito realizado correctamente. cuentaId={}, monto={}",
                cuentaGuardada.getId(),
                monto
        );

        return cuentaGuardada;
    }

    /**
     * Realiza una extracción sobre una cuenta financiera activa,
     * identificando al cliente que ejecuta la operación.
     *
     * <p>
     * Si el operador es un titular, solamente puede extraer de una cuenta
     * de la que figure como titular. Si el operador es un adherente,
     * solamente puede extraer de una cuenta perteneciente a su titular
     * y para la cual haya sido autorizado explícitamente.
     * </p>
     *
     * <p>
     * En una caja de ahorro los fondos disponibles corresponden al saldo
     * operativo. En una cuenta corriente también se considera el descubierto
     * autorizado como parte de los fondos disponibles.
     * </p>
     *
     * <p>
     * Cuando la operación es válida se actualiza el saldo y se registra
     * una transacción de tipo {@link TipoTransaccion#EXTRACCION},
     * almacenando además al cliente que realizó la operación.
     * </p>
     *
     * @param cuentaId identificador de la cuenta
     * @param operadorId identificador del cliente que realiza la extracción
     * @param monto monto que se desea extraer
     * @return cuenta financiera actualizada
     * @throws IllegalArgumentException si algún dato obligatorio es inválido
     * @throws RecursoNoEncontradoException si la cuenta o el operador no existen
     * @throws IllegalStateException si la cuenta no se encuentra activa
     *                               o el operador no está autorizado
     * @throws SaldoInsuficienteException si los fondos disponibles
     *                                    no alcanzan para realizar la operación
     */
    @Override
    @Transactional
    public CuentaFinanciera extraer(
            UUID cuentaId,
            UUID operadorId,
            BigDecimal monto) {

        if (cuentaId == null) {
            throw new IllegalArgumentException(
                    "El identificador de la cuenta es obligatorio"
            );
        }

        if (operadorId == null) {
            throw new IllegalArgumentException(
                    "El operador es obligatorio"
            );
        }

        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser positivo"
            );
        }

        CuentaFinanciera cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La cuenta no existe"
                        )
                );

        if (cuenta.getEstado() != EstadoCuenta.ACTIVA) {
            throw new IllegalStateException(
                    "La cuenta no está activa"
            );
        }

        Cliente operador = clienteRepository.findById(operadorId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El operador no existe"
                        )
                );

        validarOperadorExtraccion(cuenta, operador);

        BigDecimal fondosDisponibles = cuenta.getSaldoOperativo();

        if (cuenta instanceof CuentaCorriente cuentaCorriente) {
            fondosDisponibles = fondosDisponibles.add(
                    cuentaCorriente.getDescubiertoAutorizado()
            );
        }

        if (fondosDisponibles.compareTo(monto) < 0) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente"
            );
        }

        cuenta.setSaldoOperativo(
                cuenta.getSaldoOperativo().subtract(monto)
        );

        CuentaFinanciera cuentaGuardada =
                cuentaRepository.save(cuenta);

        Transaccion transaccion = Transaccion.builder()
                .fechaHora(LocalDateTime.now())
                .monto(monto)
                .tipo(TipoTransaccion.EXTRACCION)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(cuentaGuardada)
                .operador(operador)
                .build();

        transaccionRepository.save(transaccion);

        log.info(
                "Extracción realizada correctamente. cuentaId={}, operadorId={}, monto={}",
                cuentaGuardada.getId(),
                operador.getId(),
                monto
        );

        return cuentaGuardada;
    }

    /**
     * Verifica que el cliente que intenta realizar una extracción
     * esté autorizado para operar sobre la cuenta indicada.
     *
     * <p>
     * Un cliente sin titular asociado se considera titular y solamente
     * puede operar sobre cuentas en las que figure como titular.
     * </p>
     *
     * <p>
     * Un cliente con titular asociado se considera adherente y solamente
     * puede operar sobre cuentas que hayan sido autorizadas explícitamente
     * para él y que continúen perteneciendo a su titular.
     * </p>
     *
     * @param cuenta cuenta sobre la que se realizará la extracción
     * @param operador cliente que intenta realizar la operación
     * @throws IllegalStateException si el operador no está autorizado
     *                               para extraer de la cuenta
     */
    private void validarOperadorExtraccion(
            CuentaFinanciera cuenta,
            Cliente operador) {

        /*
         * Caso 1: el operador es titular.
         */
        if (operador.getTitular() == null) {

            boolean esTitularDeLaCuenta =
                    cuenta.getTitulares()
                            .stream()
                            .anyMatch(titular ->
                                    titular.getId() != null
                                            && titular.getId().equals(
                                            operador.getId()
                                    )
                            );

            if (!esTitularDeLaCuenta) {
                throw new IllegalStateException(
                        "El operador no está autorizado para extraer de esta cuenta"
                );
            }

            return;
        }

        /*
         * Caso 2: el operador es adherente.
         * La cuenta debe haber sido autorizada explícitamente.
         */
        boolean cuentaAutorizada =
                operador.getCuentasAutorizadas()
                        .stream()
                        .anyMatch(cuentaPermitida ->
                                cuentaPermitida.getId() != null
                                        && cuentaPermitida.getId().equals(
                                        cuenta.getId()
                                )
                        );

        if (!cuentaAutorizada) {
            throw new IllegalStateException(
                    "El adherente no está autorizado para extraer de esta cuenta"
            );
        }

        /*
         * Además verificamos que la cuenta continúe perteneciendo
         * al titular del adherente.
         */
        UUID titularId =
                operador.getTitular().getId();

        boolean cuentaPerteneceAlTitular =
                cuenta.getTitulares()
                        .stream()
                        .anyMatch(titular ->
                                titular.getId() != null
                                        && titular.getId().equals(
                                        titularId
                                )
                        );

        if (!cuentaPerteneceAlTitular) {
            throw new IllegalStateException(
                    "La cuenta autorizada ya no pertenece al titular del adherente"
            );
        }
    }

    /**
     * Verifica que el CBU y el alias de la nueva cuenta
     * no se encuentren registrados previamente.
     *
     * @param request datos de la cuenta que se desea registrar
     * @throws IllegalArgumentException si el CBU o el alias ya existen
     */
    private void validarDuplicados(CuentaRequestDto request) {
        if (cuentaRepository.findByCbu(request.getCbu()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con el CBU indicado"
            );
        }

        if (cuentaRepository.findByAlias(request.getAlias()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una cuenta con el alias indicado"
            );
        }
    }

    /**
     * Construye el subtipo de cuenta solicitado.
     *
     * <p>
     * Para una caja de ahorro se requieren la tasa de interés anual
     * y el límite de extracciones mensuales sin costo. Para una cuenta
     * corriente se requieren el descubierto autorizado y el costo
     * mensual de mantenimiento.
     * </p>
     *
     * @param request DTO con los datos de creación de la cuenta
     * @return instancia concreta de {@link CajaDeAhorro}
     * o {@link CuentaCorriente}
     * @throws IllegalArgumentException si faltan los datos específicos
     *                                  del tipo solicitado
     */
    private CuentaFinanciera crearCuentaSegunTipo(
            CuentaRequestDto request) {

        if (request.getTipoCuenta() == TipoCuenta.CAJA_DE_AHORRO) {
            if (request.getTasaInteresAnual() == null
                    || request.getLimiteExtraccionesMensualesSinCosto() == null) {

                throw new IllegalArgumentException(
                        "La caja de ahorro requiere tasa de interés anual "
                                + "y límite de extracciones mensuales sin costo"
                );
            }

            CajaDeAhorro cajaDeAhorro = new CajaDeAhorro();
            cajaDeAhorro.setTasaInteresAnual(
                    request.getTasaInteresAnual()
            );
            cajaDeAhorro.setLimiteExtraccionesMensualesSinCosto(
                    request.getLimiteExtraccionesMensualesSinCosto()
            );

            return cajaDeAhorro;
        }

        if (request.getTipoCuenta() == TipoCuenta.CUENTA_CORRIENTE) {
            if (request.getDescubiertoAutorizado() == null
                    || request.getCostoComisionMantenimientoMensual() == null) {

                throw new IllegalArgumentException(
                        "La cuenta corriente requiere descubierto autorizado "
                                + "y costo de comisión mensual"
                );
            }

            CuentaCorriente cuentaCorriente = new CuentaCorriente();
            cuentaCorriente.setDescubiertoAutorizado(
                    request.getDescubiertoAutorizado()
            );
            cuentaCorriente.setCostoComisionMantenimientoMensual(
                    request.getCostoComisionMantenimientoMensual()
            );

            return cuentaCorriente;
        }

        throw new IllegalArgumentException(
                "Tipo de cuenta no soportado"
        );
    }

    /**
     * Convierte una entidad {@link CuentaFinanciera} en el DTO
     * utilizado como respuesta de la API.
     *
     * <p>
     * El tipo de cuenta se determina a partir del subtipo concreto
     * de la entidad recuperada.
     * </p>
     *
     * @param cuenta entidad que se desea transformar
     * @return DTO con los datos públicos de la cuenta financiera
     * @throws IllegalStateException si se recibe un subtipo de cuenta
     *                               desconocido por el sistema
     */
    private CuentaResponseDto mapearAResponse(
            CuentaFinanciera cuenta) {

        TipoCuenta tipoCuenta;

        if (cuenta instanceof CajaDeAhorro) {
            tipoCuenta = TipoCuenta.CAJA_DE_AHORRO;
        } else if (cuenta instanceof CuentaCorriente) {
            tipoCuenta = TipoCuenta.CUENTA_CORRIENTE;
        } else {
            throw new IllegalStateException(
                    "Tipo de cuenta financiera desconocido"
            );
        }

        return new CuentaResponseDto(
                cuenta.getId(),
                tipoCuenta,
                cuenta.getCbu(),
                cuenta.getAlias(),
                cuenta.getSaldoOperativo(),
                cuenta.getEstado()
        );
    }
}