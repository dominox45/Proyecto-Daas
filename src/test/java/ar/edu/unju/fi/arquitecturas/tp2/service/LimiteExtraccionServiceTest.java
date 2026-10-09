package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.impl.CuentaFinancieraServiceImpl;
import ar.edu.unju.fi.arquitecturas.tp2.util.ClavesConfiguracion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias correspondientes a los límites diarios
 * acumulados de extracción del TP5.
 */
@ExtendWith(MockitoExtension.class)
class LimiteExtraccionServiceTest {

    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ConfiguracionService configuracionService;

    private CuentaFinancieraServiceImpl cuentaService;

    @BeforeEach
    void configurarServicio() {
        cuentaService = new CuentaFinancieraServiceImpl(
                cuentaRepository,
                transaccionRepository,
                clienteRepository,
                configuracionService
        );
    }

    /**
     * Verifica que un titular pueda extraer mientras el total diario
     * permanezca por debajo de su límite configurado.
     */
    @Test
    void deberiaPermitirTitularPorDebajoDelLimite() {
        UUID titularId = UUID.randomUUID();
        CuentaCorriente cuenta = prepararCuentaDeTitular(
                titularId,
                new BigDecimal("200000.00")
        );

        prepararExtraccion(
                cuenta,
                titularId,
                new BigDecimal("100000.00"),
                new BigDecimal("20000.00")
        );

        cuentaService.extraer(
                cuenta.getId(),
                titularId,
                new BigDecimal("30000.00")
        );

        assertEquals(
                new BigDecimal("170000.00"),
                cuenta.getSaldoOperativo()
        );

        verify(configuracionService)
                .obtenerValorDecimal(
                        ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR
                );
    }

    /**
     * Alcanzar exactamente el límite diario del titular debe estar permitido.
     */
    @Test
    void deberiaPermitirTitularAlcanzandoExactamenteElLimite() {
        UUID titularId = UUID.randomUUID();
        CuentaCorriente cuenta = prepararCuentaDeTitular(
                titularId,
                new BigDecimal("200000.00")
        );

        prepararExtraccion(
                cuenta,
                titularId,
                new BigDecimal("100000.00"),
                new BigDecimal("60000.00")
        );

        cuentaService.extraer(
                cuenta.getId(),
                titularId,
                new BigDecimal("40000.00")
        );

        assertEquals(
                new BigDecimal("160000.00"),
                cuenta.getSaldoOperativo()
        );
    }

    /**
     * Superar el límite del titular debe rechazar la extracción
     * antes de modificar el saldo.
     */
    @Test
    void deberiaRechazarTitularQueSuperaLimite() {
        UUID titularId = UUID.randomUUID();
        CuentaCorriente cuenta = prepararCuentaDeTitular(
                titularId,
                new BigDecimal("200000.00")
        );

        prepararExtraccion(
                cuenta,
                titularId,
                new BigDecimal("100000.00"),
                new BigDecimal("90000.00")
        );

        assertThrows(
                IllegalStateException.class,
                () -> cuentaService.extraer(
                        cuenta.getId(),
                        titularId,
                        new BigDecimal("10000.01")
                )
        );

        assertEquals(
                new BigDecimal("200000.00"),
                cuenta.getSaldoOperativo()
        );

        verify(cuentaRepository, never()).save(any());
        verify(transaccionRepository, never())
                .save(any(Transaccion.class));
    }

    /**
     * Verifica que un adherente utilice su límite independiente.
     */
    @Test
    void deberiaPermitirAdherentePorDebajoDelLimite() {
        UUID adherenteId = UUID.randomUUID();

        ContextoAdherente contexto =
                prepararCuentaDeAdherente(
                        adherenteId,
                        new BigDecimal("150000.00")
                );

        prepararExtraccion(
                contexto.cuenta(),
                adherenteId,
                new BigDecimal("70000.00"),
                new BigDecimal("20000.00")
        );

        cuentaService.extraer(
                contexto.cuenta().getId(),
                adherenteId,
                new BigDecimal("30000.00")
        );

        assertEquals(
                new BigDecimal("120000.00"),
                contexto.cuenta().getSaldoOperativo()
        );

        verify(configuracionService)
                .obtenerValorDecimal(
                        ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE
                );
    }

    /**
     * Alcanzar exactamente el límite diario del adherente debe permitirse.
     */
    @Test
    void deberiaPermitirAdherenteAlcanzandoExactamenteElLimite() {
        UUID adherenteId = UUID.randomUUID();

        ContextoAdherente contexto =
                prepararCuentaDeAdherente(
                        adherenteId,
                        new BigDecimal("150000.00")
                );

        prepararExtraccion(
                contexto.cuenta(),
                adherenteId,
                new BigDecimal("70000.00"),
                new BigDecimal("50000.00")
        );

        cuentaService.extraer(
                contexto.cuenta().getId(),
                adherenteId,
                new BigDecimal("20000.00")
        );

        assertEquals(
                new BigDecimal("130000.00"),
                contexto.cuenta().getSaldoOperativo()
        );
    }

    /**
     * Superar el límite diario del adherente debe rechazar la operación.
     */
    @Test
    void deberiaRechazarAdherenteQueSuperaLimite() {
        UUID adherenteId = UUID.randomUUID();

        ContextoAdherente contexto =
                prepararCuentaDeAdherente(
                        adherenteId,
                        new BigDecimal("150000.00")
                );

        prepararExtraccion(
                contexto.cuenta(),
                adherenteId,
                new BigDecimal("70000.00"),
                new BigDecimal("65000.00")
        );

        assertThrows(
                IllegalStateException.class,
                () -> cuentaService.extraer(
                        contexto.cuenta().getId(),
                        adherenteId,
                        new BigDecimal("5000.01")
                )
        );

        assertEquals(
                new BigDecimal("150000.00"),
                contexto.cuenta().getSaldoOperativo()
        );

        verify(cuentaRepository, never()).save(any());
        verify(transaccionRepository, never())
                .save(any(Transaccion.class));
    }

    /**
     * Comprueba que el acumulado consultado sea global para el operador
     * y no dependa de la cuenta utilizada.
     */
    @Test
    void deberiaAcumularExtraccionesDeDistintasCuentas() {
        UUID titularId = UUID.randomUUID();

        CuentaCorriente segundaCuenta =
                prepararCuentaDeTitular(
                        titularId,
                        new BigDecimal("100000.00")
                );

        prepararExtraccion(
                segundaCuenta,
                titularId,
                new BigDecimal("100000.00"),
                new BigDecimal("60000.00")
        );

        cuentaService.extraer(
                segundaCuenta.getId(),
                titularId,
                new BigDecimal("40000.00")
        );

        verify(transaccionRepository)
                .sumarMontoPorOperadorTipoEstadoYPeriodo(
                        eq(titularId),
                        eq(TipoTransaccion.EXTRACCION),
                        eq(EstadoTransaccion.COMPLETADA),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }

    /**
     * El consumo del límite de un adherente no debe afectar a otro.
     */
    @Test
    void deberiaMantenerLimitesIndependientesEntreAdherentes() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteAId = UUID.randomUUID();
        UUID adherenteBId = UUID.randomUUID();

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        Cliente adherenteA = Cliente.builder()
                .id(adherenteAId)
                .titular(titular)
                .build();

        Cliente adherenteB = Cliente.builder()
                .id(adherenteBId)
                .titular(titular)
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(UUID.randomUUID());
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("200000.00"));
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.getTitulares().add(titular);

        adherenteA.autorizarCuenta(cuenta);
        adherenteB.autorizarCuenta(cuenta);

        when(cuentaRepository.findById(cuenta.getId()))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(adherenteAId))
                .thenReturn(Optional.of(adherenteA));

        when(clienteRepository.findById(adherenteBId))
                .thenReturn(Optional.of(adherenteB));

        when(configuracionService.obtenerValorDecimal(
                ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE
        )).thenReturn(new BigDecimal("70000.00"));

        when(transaccionRepository
                .sumarMontoPorOperadorTipoEstadoYPeriodo(
                        eq(adherenteAId),
                        eq(TipoTransaccion.EXTRACCION),
                        eq(EstadoTransaccion.COMPLETADA),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(new BigDecimal("70000.00"));

        when(transaccionRepository
                .sumarMontoPorOperadorTipoEstadoYPeriodo(
                        eq(adherenteBId),
                        eq(TipoTransaccion.EXTRACCION),
                        eq(EstadoTransaccion.COMPLETADA),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(BigDecimal.ZERO);

        assertThrows(
                IllegalStateException.class,
                () -> cuentaService.extraer(
                        cuenta.getId(),
                        adherenteAId,
                        new BigDecimal("1.00")
                )
        );

        when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        cuentaService.extraer(
                cuenta.getId(),
                adherenteBId,
                new BigDecimal("1.00")
        );

        assertEquals(
                new BigDecimal("199999.00"),
                cuenta.getSaldoOperativo()
        );
    }

    /**
     * Configura una cuenta perteneciente a un titular.
     */
    private CuentaCorriente prepararCuentaDeTitular(
            UUID titularId,
            BigDecimal saldo) {

        Cliente titular = Cliente.builder()
                .id(titularId)
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(UUID.randomUUID());
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(saldo);
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.getTitulares().add(titular);

        when(cuentaRepository.findById(cuenta.getId()))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(titularId))
                .thenReturn(Optional.of(titular));

        lenient()
                .when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        return cuenta;
    }

    /**
     * Configura una cuenta y un cliente adherente autorizado para operarla.
     */
    private ContextoAdherente prepararCuentaDeAdherente(
            UUID adherenteId,
            BigDecimal saldo) {

        Cliente titular = Cliente.builder()
                .id(UUID.randomUUID())
                .build();

        Cliente adherente = Cliente.builder()
                .id(adherenteId)
                .titular(titular)
                .build();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(UUID.randomUUID());
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(saldo);
        cuenta.setDescubiertoAutorizado(BigDecimal.ZERO);
        cuenta.getTitulares().add(titular);

        adherente.autorizarCuenta(cuenta);

        when(cuentaRepository.findById(cuenta.getId()))
                .thenReturn(Optional.of(cuenta));

        when(clienteRepository.findById(adherenteId))
                .thenReturn(Optional.of(adherente));

        lenient()
                .when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        return new ContextoAdherente(cuenta, adherente);
    }

    /**
     * Configura el límite dinámico y el acumulado previo del operador.
     */
    private void prepararExtraccion(
            CuentaCorriente cuenta,
            UUID operadorId,
            BigDecimal limite,
            BigDecimal acumulado) {

        Cliente operador = clienteRepository
                .findById(operadorId)
                .orElseThrow();

        String clave = operador.getTitular() == null
                ? ClavesConfiguracion.LIMITE_EXTRACCION_TITULAR
                : ClavesConfiguracion.LIMITE_EXTRACCION_ADHERENTE;

        when(configuracionService.obtenerValorDecimal(clave))
                .thenReturn(limite);

        when(transaccionRepository
                .sumarMontoPorOperadorTipoEstadoYPeriodo(
                        eq(operadorId),
                        eq(TipoTransaccion.EXTRACCION),
                        eq(EstadoTransaccion.COMPLETADA),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(acumulado);
    }

    /**
     * Agrupa los objetos requeridos para probar un adherente.
     */
    private record ContextoAdherente(
            CuentaCorriente cuenta,
            Cliente adherente) {
    }
}