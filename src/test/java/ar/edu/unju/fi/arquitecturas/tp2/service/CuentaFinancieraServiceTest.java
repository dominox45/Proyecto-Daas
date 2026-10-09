package ar.edu.unju.fi.arquitecturas.tp2.service;

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
import ar.edu.unju.fi.arquitecturas.tp2.service.impl.CuentaFinancieraServiceImpl;
import ar.edu.unju.fi.arquitecturas.tp2.util.ClavesConfiguracion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para {@link CuentaFinancieraServiceImpl}.
 *
 * <p>
 * Verifica las reglas de negocio relacionadas con la creación,
 * consulta, depósito y extracción de fondos sobre cuentas financieras.
 * </p>
 *
 * <p>
 * Los repositorios son simulados mediante Mockito para aislar
 * el comportamiento de la capa Service y evitar dependencias
 * con una base de datos real.
 * </p>
 *
 * @author MaxDz
 * @version 1.2.0
 * @see CuentaFinancieraServiceImpl
 * @see CuentaFinancieraRepository
 * @see TransaccionRepository
 * @see ClienteRepository
 */
@ExtendWith(MockitoExtension.class)
class CuentaFinancieraServiceTest {

    /**
     * Repositorio simulado para las operaciones relacionadas
     * con cuentas financieras.
     */
    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    /**
     * Repositorio simulado para las operaciones relacionadas
     * con transacciones.
     */
    @Mock
    private TransaccionRepository transaccionRepository;

    /**
     * Repositorio simulado para recuperar clientes y mantener
     * la asociación entre titulares y cuentas.
     */
    @Mock
    private ClienteRepository clienteRepository;

    /**
     * Servicio simulado para recuperar los límites dinámicos
     * de extracción.
     */
    @Mock
    private ConfiguracionService configuracionService;

    /**
     * Servicio bajo prueba con las dependencias simuladas
     * inyectadas automáticamente por Mockito.
     */
    @InjectMocks
    private CuentaFinancieraServiceImpl cuentaService;

    /**
     * Configura valores predeterminados para las pruebas de extracción
     * existentes, evitando que la nueva regla de límites diarios
     * modifique el objetivo original de esos casos de prueba.
     */
    @BeforeEach
    void configurarLimitesDeExtraccionPorDefecto() {

        lenient()
                .when(configuracionService.obtenerValorDecimal(
                        ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR
                ))
                .thenReturn(new BigDecimal("100000.00"));

        lenient()
                .when(configuracionService.obtenerValorDecimal(
                        ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE
                ))
                .thenReturn(new BigDecimal("70000.00"));

        lenient()
                .when(transaccionRepository
                        .sumarMontoPorOperadorTipoEstadoYPeriodo(
                                any(),
                                any(),
                                any(),
                                any(),
                                any()
                        ))
                .thenReturn(BigDecimal.ZERO);
    }

    /**
     * Verifica que pueda crearse correctamente una caja de ahorro
     * asociada a un cliente existente.
     *
     * <p>
     * La nueva cuenta debe comenzar con saldo cero, estado activo
     * y conservar los atributos específicos de una caja de ahorro.
     * </p>
     */
    @Test
    void deberiaCrearCajaDeAhorro() {
        UUID clienteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .nombre("Ana López")
                .cuil("27304050608")
                .build();

        CuentaRequestDto request = new CuentaRequestDto(
                clienteId,
                TipoCuenta.CAJA_DE_AHORRO,
                "2850590940090418135201",
                "ana.ahorro",
                new BigDecimal("0.050000"),
                5,
                null,
                null
        );

        when(clienteRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));

        when(cuentaRepository.findByCbu(request.getCbu()))
                .thenReturn(Optional.empty());

        when(cuentaRepository.findByAlias(request.getAlias()))
                .thenReturn(Optional.empty());

        when(cuentaRepository.save(any(CuentaFinanciera.class)))
                .thenAnswer(invocation -> {
                    CuentaFinanciera cuenta =
                            invocation.getArgument(0);
                    cuenta.setId(cuentaId);
                    return cuenta;
                });

        CuentaResponseDto resultado =
                cuentaService.crear(request);

        assertEquals(cuentaId, resultado.getId());
        assertEquals(
                TipoCuenta.CAJA_DE_AHORRO,
                resultado.getTipoCuenta()
        );
        assertEquals(request.getCbu(), resultado.getCbu());
        assertEquals(request.getAlias(), resultado.getAlias());
        assertEquals(
                BigDecimal.ZERO,
                resultado.getSaldoOperativo()
        );
        assertEquals(
                EstadoCuenta.ACTIVA,
                resultado.getEstado()
        );

        ArgumentCaptor<CuentaFinanciera> captor =
                ArgumentCaptor.forClass(
                        CuentaFinanciera.class
                );

        verify(cuentaRepository)
                .save(captor.capture());

        CuentaFinanciera cuentaCreada =
                captor.getValue();

        CajaDeAhorro caja =
                assertInstanceOf(
                        CajaDeAhorro.class,
                        cuentaCreada
                );

        assertEquals(
                request.getTasaInteresAnual(),
                caja.getTasaInteresAnual()
        );

        assertEquals(
                request.getLimiteExtraccionesMensualesSinCosto()
                        .intValue(),
                caja.getLimiteExtraccionesMensualesSinCosto()
        );

        assertTrue(
                cliente.getCuentas().contains(cuentaCreada)
        );

        verify(clienteRepository).save(cliente);
    }

    /**
     * Verifica que pueda crearse correctamente una cuenta corriente
     * asociada a un cliente existente.
     *
     * <p>
     * Se comprueba además que el descubierto autorizado y el costo
     * mensual de mantenimiento sean asignados correctamente.
     * </p>
     */
    @Test
    void deberiaCrearCuentaCorriente() {
        UUID clienteId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .nombre("Juan Pérez")
                .cuil("20304050607")
                .build();

        CuentaRequestDto request = new CuentaRequestDto(
                clienteId,
                TipoCuenta.CUENTA_CORRIENTE,
                "2850590940090418135202",
                "juan.corriente",
                null,
                null,
                new BigDecimal("5000.00"),
                new BigDecimal("1500.00")
        );

        when(clienteRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));

        when(cuentaRepository.findByCbu(request.getCbu()))
                .thenReturn(Optional.empty());

        when(cuentaRepository.findByAlias(request.getAlias()))
                .thenReturn(Optional.empty());

        when(cuentaRepository.save(any(CuentaFinanciera.class)))
                .thenAnswer(invocation -> {
                    CuentaFinanciera cuenta =
                            invocation.getArgument(0);
                    cuenta.setId(cuentaId);
                    return cuenta;
                });

        CuentaResponseDto resultado =
                cuentaService.crear(request);

        assertEquals(cuentaId, resultado.getId());
        assertEquals(
                TipoCuenta.CUENTA_CORRIENTE,
                resultado.getTipoCuenta()
        );
        assertEquals(
                BigDecimal.ZERO,
                resultado.getSaldoOperativo()
        );
        assertEquals(
                EstadoCuenta.ACTIVA,
                resultado.getEstado()
        );

        ArgumentCaptor<CuentaFinanciera> captor =
                ArgumentCaptor.forClass(
                        CuentaFinanciera.class
                );

        verify(cuentaRepository)
                .save(captor.capture());

        CuentaCorriente cuentaCorriente =
                assertInstanceOf(
                        CuentaCorriente.class,
                        captor.getValue()
                );

        assertEquals(
                request.getDescubiertoAutorizado(),
                cuentaCorriente.getDescubiertoAutorizado()
        );

        assertEquals(
                request.getCostoComisionMantenimientoMensual(),
                cuentaCorriente
                        .getCostoComisionMantenimientoMensual()
        );

        assertTrue(
                cliente.getCuentas()
                        .contains(cuentaCorriente)
        );

        verify(clienteRepository).save(cliente);
    }

    /**
     * Verifica que un cliente adherente no pueda crear una cuenta
     * financiera actuando como titular.
     */
    @Test
    void deberiaRechazarCreacionDeCuentaParaAdherente() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        CuentaRequestDto request = new CuentaRequestDto(
                adherenteId,
                TipoCuenta.CAJA_DE_AHORRO,
                "2850590940090418135299",
                "adherente.ahorro",
                new BigDecimal("0.050000"),
                5,
                null,
                null
        );

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> cuentaService.crear(request)
        );

        assertEquals(
                "Los clientes adherentes solo pueden realizar extracciones",
                excepcion.getMessage()
        );

        verify(clienteRepository).findById(adherenteId);

        verifyNoInteractions(
                cuentaRepository,
                transaccionRepository
        );
    }

    /**
     * Verifica que no pueda crearse una cuenta cuando el cliente
     * indicado en la solicitud no existe.
     */
    @Test
    void deberiaRechazarCreacionCuandoClienteNoExiste() {
        UUID clienteId = UUID.randomUUID();

        CuentaRequestDto request = new CuentaRequestDto(
                clienteId,
                TipoCuenta.CAJA_DE_AHORRO,
                "2850590940090418135203",
                "cliente.inexistente",
                new BigDecimal("0.050000"),
                5,
                null,
                null
        );

        when(clienteRepository.findById(clienteId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> cuentaService.crear(request)
        );

        verify(clienteRepository)
                .findById(clienteId);

        verify(
                cuentaRepository,
                never()
        ).save(any());
    }

    /**
     * Verifica que no pueda registrarse una cuenta cuyo CBU
     * ya se encuentre utilizado por otra cuenta.
     */
    @Test
    void deberiaRechazarCbuDuplicado() {
        UUID clienteId = UUID.randomUUID();

        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .build();

        CuentaRequestDto request = new CuentaRequestDto(
                clienteId,
                TipoCuenta.CAJA_DE_AHORRO,
                "2850590940090418135204",
                "nuevo.alias",
                new BigDecimal("0.050000"),
                5,
                null,
                null
        );

        CuentaCorriente cuentaExistente =
                new CuentaCorriente();

        when(clienteRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));

        when(cuentaRepository.findByCbu(request.getCbu()))
                .thenReturn(Optional.of(cuentaExistente));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cuentaService.crear(request)
                );

        assertEquals(
                "Ya existe una cuenta con el CBU indicado",
                excepcion.getMessage()
        );

        verify(
                cuentaRepository,
                never()
        ).save(any());
    }

    /**
     * Verifica que no pueda registrarse una cuenta cuyo alias
     * ya se encuentre utilizado por otra cuenta.
     */
    @Test
    void deberiaRechazarAliasDuplicado() {
        UUID clienteId = UUID.randomUUID();

        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .build();

        CuentaRequestDto request = new CuentaRequestDto(
                clienteId,
                TipoCuenta.CAJA_DE_AHORRO,
                "2850590940090418135205",
                "alias.duplicado",
                new BigDecimal("0.050000"),
                5,
                null,
                null
        );

        CuentaCorriente cuentaExistente =
                new CuentaCorriente();

        when(clienteRepository.findById(clienteId))
                .thenReturn(Optional.of(cliente));

        when(cuentaRepository.findByCbu(request.getCbu()))
                .thenReturn(Optional.empty());

        when(cuentaRepository.findByAlias(request.getAlias()))
                .thenReturn(Optional.of(cuentaExistente));

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> cuentaService.crear(request)
                );

        assertEquals(
                "Ya existe una cuenta con el alias indicado",
                excepcion.getMessage()
        );

        verify(
                cuentaRepository,
                never()
        ).save(any());
    }

    /**
     * Verifica que una cuenta pueda consultarse correctamente
     * mediante su CBU.
     */
    @Test
    void deberiaBuscarCuentaPorCbu() {
        UUID cuentaId = UUID.randomUUID();
        String cbu = "2850590940090418135206";

        CajaDeAhorro cuenta = new CajaDeAhorro();
        cuenta.setId(cuentaId);
        cuenta.setCbu(cbu);
        cuenta.setAlias("consulta.ahorro");
        cuenta.setSaldoOperativo(
                new BigDecimal("2500.00")
        );
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setTasaInteresAnual(
                new BigDecimal("0.050000")
        );
        cuenta.setLimiteExtraccionesMensualesSinCosto(5);

        when(cuentaRepository.findByCbu(cbu))
                .thenReturn(Optional.of(cuenta));

        CuentaResponseDto resultado =
                cuentaService.buscarPorCbu(cbu);

        assertEquals(cuentaId, resultado.getId());
        assertEquals(
                TipoCuenta.CAJA_DE_AHORRO,
                resultado.getTipoCuenta()
        );
        assertEquals(cbu, resultado.getCbu());
        assertEquals(
                "consulta.ahorro",
                resultado.getAlias()
        );
        assertEquals(
                new BigDecimal("2500.00"),
                resultado.getSaldoOperativo()
        );
        assertEquals(
                EstadoCuenta.ACTIVA,
                resultado.getEstado()
        );

        verify(cuentaRepository)
                .findByCbu(cbu);
    }

    /**
     * Verifica que la búsqueda por CBU produzca una excepción
     * cuando la cuenta solicitada no existe.
     */
    @Test
    void deberiaRechazarBusquedaPorCbuInexistente() {
        String cbu = "2850590940090418135207";

        when(cuentaRepository.findByCbu(cbu))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> cuentaService.buscarPorCbu(cbu)
        );

        verify(cuentaRepository)
                .findByCbu(cbu);
    }

    /**
     * Verifica que un depósito válido incremente correctamente
     * el saldo de una cuenta activa y registre una transacción
     * de tipo depósito.
     */
    @Test
    void deberiaDepositarYRegistrarTransaccion() {
        UUID cuentaId = UUID.randomUUID();
        BigDecimal monto =
                new BigDecimal("1500.00");

        CuentaCorriente cuenta =
                new CuentaCorriente();

        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(
                new BigDecimal("10000.00")
        );

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        var resultado =
                cuentaService.depositar(
                        cuentaId,
                        monto
                );

        assertEquals(
                new BigDecimal("11500.00"),
                resultado.getSaldoOperativo()
        );

        verify(cuentaRepository)
                .save(cuenta);

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(
                        Transaccion.class
                );

        verify(transaccionRepository)
                .save(captor.capture());

        Transaccion transaccion =
                captor.getValue();

        assertEquals(
                monto,
                transaccion.getMonto()
        );

        assertEquals(
                TipoTransaccion.DEPOSITO,
                transaccion.getTipo()
        );

        assertEquals(
                EstadoTransaccion.COMPLETADA,
                transaccion.getEstadoTransaccion()
        );

        assertSame(
                cuenta,
                transaccion.getCuenta()
        );

        assertNotNull(
                transaccion.getFechaHora()
        );
    }

    /**
     * Verifica que un depósito sea rechazado cuando el monto
     * proporcionado es negativo.
     */
    @Test
    void deberiaRechazarMontoNegativo() {
        UUID cuentaId = UUID.randomUUID();
        BigDecimal monto =
                new BigDecimal("-100.00");

        assertThrows(
                IllegalArgumentException.class,
                () -> cuentaService.depositar(
                        cuentaId,
                        monto
                )
        );

        verifyNoInteractions(
                cuentaRepository,
                transaccionRepository
        );
    }

    /**
     * Verifica que un depósito sea rechazado cuando la cuenta
     * solicitada no existe.
     */
    @Test
    void deberiaRechazarCuentaInexistente() {
        UUID cuentaId = UUID.randomUUID();

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> cuentaService.depositar(
                        cuentaId,
                        new BigDecimal("100.00")
                )
        );

        verify(cuentaRepository)
                .findById(cuentaId);

        verify(
                cuentaRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transaccionRepository
        );
    }

    /**
     * Verifica que no puedan realizarse depósitos sobre una
     * cuenta cuyo estado no sea activo.
     */
    @Test
    void deberiaRechazarCuentaInactiva() {
        UUID cuentaId = UUID.randomUUID();

        CuentaCorriente cuenta =
                new CuentaCorriente();

        cuenta.setEstado(
                EstadoCuenta.BLOQUEADA
        );

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        assertThrows(
                IllegalStateException.class,
                () -> cuentaService.depositar(
                        cuentaId,
                        new BigDecimal("100.00")
                )
        );

        verify(cuentaRepository)
                .findById(cuentaId);

        verify(
                cuentaRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transaccionRepository
        );
    }

    /**
     * Verifica que un cliente titular pueda extraer de una cuenta
     * de la que figura como titular y que la transacción registre
     * correctamente al operador.
     */
    @Test
    void deberiaExtraerComoTitularYRegistrarOperador() {
        UUID cuentaId = UUID.randomUUID();
        UUID titularId = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("300.00");

        Cliente titular = Cliente.builder()
                .id(titularId)
                .nombre("Juan Pérez")
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("1000.00"));
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.getTitulares().add(titular);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        CuentaFinanciera resultado = cuentaService.extraer(
                cuentaId,
                titularId,
                monto
        );

        assertEquals(
                new BigDecimal("700.00"),
                resultado.getSaldoOperativo()
        );

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(Transaccion.class);

        verify(cuentaRepository).save(cuenta);
        verify(transaccionRepository).save(captor.capture());

        Transaccion transaccion = captor.getValue();

        assertEquals(monto, transaccion.getMonto());
        assertEquals(
                TipoTransaccion.EXTRACCION,
                transaccion.getTipo()
        );
        assertEquals(
                EstadoTransaccion.COMPLETADA,
                transaccion.getEstadoTransaccion()
        );
        assertSame(cuenta, transaccion.getCuenta());
        assertSame(titular, transaccion.getOperador());
        assertNotNull(transaccion.getFechaHora());
    }

    /**
     * Verifica que un cliente adherente pueda realizar una extracción
     * sobre una cuenta perteneciente a su titular.
     */
    @Test
    void deberiaPermitirExtraccionDeAdherenteSobreCuentaDelTitular() {
        UUID cuentaId = UUID.randomUUID();
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .nombre("Juan Pérez")
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .nombre("María Pérez")
                .titular(titular)
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("1000.00"));
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.getTitulares().add(titular);
        adherente.autorizarCuenta(cuenta);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        cuentaService.extraer(
                cuentaId,
                adherenteId,
                new BigDecimal("250.00")
        );

        assertEquals(
                new BigDecimal("750.00"),
                cuenta.getSaldoOperativo()
        );

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(Transaccion.class);

        verify(transaccionRepository).save(captor.capture());

        assertSame(
                adherente,
                captor.getValue().getOperador()
        );
    }

    /**
     * Verifica que un adherente no pueda extraer de una cuenta
     * que pertenece a su titular pero que no fue autorizada
     * explícitamente para él.
     */
    @Test
    void deberiaRechazarAdherenteSobreCuentaNoAutorizada() {
        UUID cuentaId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(UUID.randomUUID())
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("1000.00"));
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);

        /*
         * La cuenta pertenece efectivamente al titular,
         * pero no fue agregada a cuentasAutorizadas del adherente.
         */
        cuenta.getTitulares().add(titular);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> cuentaService.extraer(
                        cuentaId,
                        adherenteId,
                        new BigDecimal("100.00")
                )
        );

        assertEquals(
                "El adherente no está autorizado para extraer de esta cuenta",
                excepcion.getMessage()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                cuenta.getSaldoOperativo()
        );

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    /**
     * Verifica que una autorización previa deje de ser válida
     * si la cuenta ya no pertenece al titular del adherente.
     */
    @Test
    void deberiaRechazarCuentaAutorizadaQueYaNoPerteneceAlTitular() {
        UUID cuentaId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(UUID.randomUUID())
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        Cliente titularAjeno = Cliente.builder()
                .id(UUID.randomUUID())
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("1000.00"));
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);

        /*
         * La autorización existe todavía en el adherente...
         */
        adherente.autorizarCuenta(cuenta);

        /*
         * ...pero actualmente la cuenta pertenece a otra persona.
         */
        cuenta.getTitulares().add(titularAjeno);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> cuentaService.extraer(
                        cuentaId,
                        adherenteId,
                        new BigDecimal("100.00")
                )
        );

        assertEquals(
                "La cuenta autorizada ya no pertenece al titular del adherente",
                excepcion.getMessage()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                cuenta.getSaldoOperativo()
        );

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    /**
     * Verifica que un titular no pueda extraer de una cuenta
     * perteneciente exclusivamente a otro titular.
     */
    @Test
    void deberiaRechazarTitularSobreCuentaAjena() {
        UUID cuentaId = UUID.randomUUID();
        UUID operadorId = UUID.randomUUID();

        Cliente operador = Cliente.builder()
                .id(operadorId)
                .build();

        Cliente titularAjeno = Cliente.builder()
                .id(UUID.randomUUID())
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("1000.00"));
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.getTitulares().add(titularAjeno);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(operadorId))
                .thenReturn(Optional.of(operador));

        assertThrows(
                IllegalStateException.class,
                () -> cuentaService.extraer(
                        cuentaId,
                        operadorId,
                        new BigDecimal("100.00")
                )
        );

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    /**
     * Verifica que la extracción sea rechazada cuando el cliente
     * indicado como operador no existe.
     */
    @Test
    void deberiaRechazarExtraccionCuandoOperadorNoExiste() {
        UUID cuentaId = UUID.randomUUID();
        UUID operadorId = UUID.randomUUID();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("1000.00"));
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(operadorId))
                .thenReturn(Optional.empty());

        RecursoNoEncontradoException excepcion = assertThrows(
                RecursoNoEncontradoException.class,
                () -> cuentaService.extraer(
                        cuentaId,
                        operadorId,
                        new BigDecimal("100.00")
                )
        );

        assertEquals(
                "El operador no existe",
                excepcion.getMessage()
        );

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    /**
     * Verifica que una cuenta corriente pueda utilizar su descubierto
     * autorizado cuando el saldo operativo no alcanza por sí solo.
     */
    @Test
    void deberiaPermitirExtraccionUsandoDescubiertoAutorizado() {
        UUID cuentaId = UUID.randomUUID();
        UUID titularId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("1000.00"));
        cuenta.setDescubiertoAutorizado(new BigDecimal("500.00"));
        cuenta.getTitulares().add(titular);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        CuentaFinanciera resultado = cuentaService.extraer(
                cuentaId,
                titularId,
                new BigDecimal("1200.00")
        );

        assertEquals(
                new BigDecimal("-200.00"),
                resultado.getSaldoOperativo()
        );

        verify(cuentaRepository).save(cuenta);
        verify(transaccionRepository)
                .save(any(Transaccion.class));
    }

    /**
     * Verifica que una extracción sea rechazada cuando los fondos
     * disponibles, incluyendo el descubierto autorizado, no alcanzan
     * para cubrir el monto solicitado.
     */
    @Test
    void deberiaRechazarExtraccionSinSaldoSuficiente() {
        UUID cuentaId = UUID.randomUUID();
        UUID titularId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("100.00"));
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.getTitulares().add(titular);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        assertThrows(
                SaldoInsuficienteException.class,
                () -> cuentaService.extraer(
                        cuentaId,
                        titularId,
                        new BigDecimal("150.00")
                )
        );

        assertEquals(
                new BigDecimal("100.00"),
                cuenta.getSaldoOperativo()
        );

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }
}