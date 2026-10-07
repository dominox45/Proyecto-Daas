package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.exception.SaldoInsuficienteException;
import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.impl.TransferenciaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.times;

/**
 * Pruebas unitarias para {@link TransferenciaServiceImpl}.
 *
 * <p>
 * Verifica las reglas de negocio, validaciones de saldo,
 * autorización del operador y persistencia asociadas
 * a las transferencias entre cuentas financieras.
 * </p>
 *
 * @author MaxDz
 * @version 1.1.0
 * @see TransferenciaServiceImpl
 * @see ClienteRepository
 * @see CuentaFinancieraRepository
 * @see TransaccionRepository
 */
@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private TransferenciaServiceImpl transferenciaService;

    /**
     * Verifica que una transferencia exitosa descuente el saldo
     * de la cuenta origen, acredite en la cuenta destino,
     * registre ambos movimientos e identifique al operador.
     */
    @Test
    void deberiaTransferirYRegistrarAmbosMovimientos() {
        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("300.00");

        CuentaCorriente origen = crearCuentaCorriente(
                origenId,
                "1000.00",
                "500.00"
        );

        CuentaCorriente destino = crearCuentaCorriente(
                destinoId,
                "500.00",
                "500.00"
        );

        Cliente operador =
                configurarTitularOperador(
                        operadorId,
                        origen
                );

        configurarCuentas(
                origenId,
                origen,
                destinoId,
                destino
        );

        var response =
                transferenciaService.transferir(
                        request(
                                operadorId,
                                origenId,
                                destinoId,
                                monto
                        )
                );

        assertEquals(
                new BigDecimal("700.00"),
                origen.getSaldoOperativo()
        );

        assertEquals(
                new BigDecimal("800.00"),
                destino.getSaldoOperativo()
        );

        assertEquals(
                origenId,
                response.getCuentaOrigenId()
        );

        assertEquals(
                destinoId,
                response.getCuentaDestinoId()
        );

        assertEquals(
                monto,
                response.getMonto()
        );

        assertEquals(
                EstadoTransaccion.COMPLETADA,
                response.getEstado()
        );

        assertNotNull(
                response.getFechaHora()
        );

        verify(cuentaRepository)
                .save(origen);

        verify(cuentaRepository)
                .save(destino);

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(
                        Transaccion.class
                );

        verify(
                transaccionRepository,
                times(2)
        ).save(captor.capture());

        List<Transaccion> transacciones =
                captor.getAllValues();

        Transaccion enviada =
                transacciones.get(0);

        Transaccion recibida =
                transacciones.get(1);

        assertEquals(
                TipoTransaccion.TRANSFERENCIA_ENVIADA,
                enviada.getTipo()
        );

        assertEquals(
                TipoTransaccion.TRANSFERENCIA_RECIBIDA,
                recibida.getTipo()
        );

        assertEquals(
                monto,
                enviada.getMonto()
        );

        assertEquals(
                monto,
                recibida.getMonto()
        );

        assertSame(
                origen,
                enviada.getCuenta()
        );

        assertSame(
                destino,
                recibida.getCuenta()
        );

        assertSame(
                operador,
                enviada.getOperador()
        );

        assertSame(
                operador,
                recibida.getOperador()
        );

        assertEquals(
                EstadoTransaccion.COMPLETADA,
                enviada.getEstadoTransaccion()
        );

        assertEquals(
                EstadoTransaccion.COMPLETADA,
                recibida.getEstadoTransaccion()
        );

        assertEquals(
                enviada.getFechaHora(),
                recibida.getFechaHora()
        );

        assertEquals(
                response.getFechaHora(),
                enviada.getFechaHora()
        );
    }

    /**
     * Verifica que una cuenta corriente pueda utilizar
     * el descubierto autorizado durante una transferencia.
     */
    @Test
    void deberiaPermitirDescubiertoEnCuentaCorriente() {
        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CuentaCorriente origen =
                crearCuentaCorriente(
                        origenId,
                        "1000.00",
                        "2000.00"
                );

        CuentaCorriente destino =
                crearCuentaCorriente(
                        destinoId,
                        "500.00",
                        "0.00"
                );

        configurarTitularOperador(
                operadorId,
                origen
        );

        configurarCuentas(
                origenId,
                origen,
                destinoId,
                destino
        );

        transferenciaService.transferir(
                request(
                        operadorId,
                        origenId,
                        destinoId,
                        new BigDecimal("2500.00")
                )
        );

        assertEquals(
                new BigDecimal("-1500.00"),
                origen.getSaldoOperativo()
        );

        assertEquals(
                new BigDecimal("3000.00"),
                destino.getSaldoOperativo()
        );
    }

    /**
     * Verifica que la transferencia sea rechazada cuando
     * se supera el saldo más el descubierto autorizado.
     */
    @Test
    void deberiaRechazarCuandoSeExcedeElDescubierto() {
        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CuentaCorriente origen =
                crearCuentaCorriente(
                        origenId,
                        "1000.00",
                        "2000.00"
                );

        CuentaCorriente destino =
                crearCuentaCorriente(
                        destinoId,
                        "500.00",
                        "0.00"
                );

        configurarTitularOperador(
                operadorId,
                origen
        );

        configurarCuentas(
                origenId,
                origen,
                destinoId,
                destino
        );

        assertThrows(
                SaldoInsuficienteException.class,
                () -> transferenciaService.transferir(
                        request(
                                operadorId,
                                origenId,
                                destinoId,
                                new BigDecimal("3000.01")
                        )
                )
        );

        assertEquals(
                new BigDecimal("1000.00"),
                origen.getSaldoOperativo()
        );

        verify(
                cuentaRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transaccionRepository
        );
    }

    /**
     * Verifica que una caja de ahorro no permita
     * transferir más dinero del saldo disponible.
     */
    @Test
    void deberiaRechazarCajaDeAhorroSinSaldo() {
        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CajaDeAhorro origen =
                new CajaDeAhorro();

        origen.setId(origenId);
        origen.setEstado(
                EstadoCuenta.ACTIVA
        );
        origen.setSaldoOperativo(
                new BigDecimal("100.00")
        );

        CuentaCorriente destino =
                crearCuentaCorriente(
                        destinoId,
                        "500.00",
                        "0.00"
                );

        configurarTitularOperador(
                operadorId,
                origen
        );

        configurarCuentas(
                origenId,
                origen,
                destinoId,
                destino
        );

        assertThrows(
                SaldoInsuficienteException.class,
                () -> transferenciaService.transferir(
                        request(
                                operadorId,
                                origenId,
                                destinoId,
                                new BigDecimal("150.00")
                        )
                )
        );

        assertEquals(
                new BigDecimal("100.00"),
                origen.getSaldoOperativo()
        );

        verify(
                cuentaRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transaccionRepository
        );
    }

    /**
     * Verifica que la transferencia sea rechazada
     * cuando la cuenta de origen no existe.
     */
    @Test
    void deberiaRechazarCuentaOrigenInexistente() {
        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        Cliente operador =
                Cliente.builder()
                        .id(operadorId)
                        .build();

        when(clienteRepository.findById(operadorId))
                .thenReturn(
                        Optional.of(operador)
                );

        when(cuentaRepository.findById(origenId))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> transferenciaService.transferir(
                        request(
                                operadorId,
                                origenId,
                                destinoId,
                                new BigDecimal("100.00")
                        )
                )
        );

        verify(cuentaRepository)
                .findById(origenId);

        verify(
                cuentaRepository,
                never()
        ).findById(destinoId);

        verifyNoInteractions(
                transaccionRepository
        );
    }

    /**
     * Verifica que la transferencia sea rechazada
     * cuando la cuenta de destino no existe.
     */
    @Test
    void deberiaRechazarCuentaDestinoInexistente() {
        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CuentaCorriente origen =
                crearCuentaCorriente(
                        origenId,
                        "1000.00",
                        "500.00"
                );

        configurarTitularOperador(
                operadorId,
                origen
        );

        when(cuentaRepository.findById(origenId))
                .thenReturn(
                        Optional.of(origen)
                );

        when(cuentaRepository.findById(destinoId))
                .thenReturn(
                        Optional.empty()
                );

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> transferenciaService.transferir(
                        request(
                                operadorId,
                                origenId,
                                destinoId,
                                new BigDecimal("100.00")
                        )
                )
        );

        verify(cuentaRepository)
                .findById(origenId);

        verify(cuentaRepository)
                .findById(destinoId);

        verify(
                cuentaRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transaccionRepository
        );
    }

    /**
     * Verifica que la cuenta de origen y destino
     * no puedan ser la misma.
     */
    @Test
    void deberiaRechazarCuentasIguales() {
        UUID operadorId = UUID.randomUUID();
        UUID cuentaId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> transferenciaService.transferir(
                        request(
                                operadorId,
                                cuentaId,
                                cuentaId,
                                new BigDecimal("100.00")
                        )
                )
        );

        verifyNoInteractions(
                clienteRepository,
                cuentaRepository,
                transaccionRepository
        );
    }

    /**
     * Verifica que el monto de la transferencia
     * deba ser estrictamente positivo.
     */
    @Test
    void deberiaRechazarMontoNoPositivo() {
        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> transferenciaService.transferir(
                        request(
                                operadorId,
                                origenId,
                                destinoId,
                                BigDecimal.ZERO
                        )
                )
        );

        verifyNoInteractions(
                clienteRepository,
                cuentaRepository,
                transaccionRepository
        );
    }

    /**
     * Verifica que sea obligatorio identificar
     * al cliente que ejecuta la transferencia.
     */
    @Test
    void deberiaRechazarTransferenciaSinOperador() {
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        IllegalArgumentException excepcion =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> transferenciaService.transferir(
                                request(
                                        null,
                                        origenId,
                                        destinoId,
                                        new BigDecimal("100.00")
                                )
                        )
                );

        assertEquals(
                "El operador es obligatorio",
                excepcion.getMessage()
        );

        verifyNoInteractions(
                clienteRepository,
                cuentaRepository,
                transaccionRepository
        );
    }

    /**
     * Verifica la regla del TP5 que establece que
     * un adherente solo puede realizar extracciones.
     */
    @Test
    void deberiaRechazarTransferenciaRealizadaPorAdherente() {
        UUID titularId = UUID.randomUUID();
        UUID adherenteId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        Cliente titular =
                Cliente.builder()
                        .id(titularId)
                        .build();

        Cliente adherente =
                Cliente.builder()
                        .id(adherenteId)
                        .titular(titular)
                        .build();

        when(clienteRepository.findById(adherenteId))
                .thenReturn(
                        Optional.of(adherente)
                );

        IllegalStateException excepcion =
                assertThrows(
                        IllegalStateException.class,
                        () -> transferenciaService.transferir(
                                request(
                                        adherenteId,
                                        origenId,
                                        destinoId,
                                        new BigDecimal("100.00")
                                )
                        )
                );

        assertEquals(
                "Los clientes adherentes solo pueden realizar extracciones",
                excepcion.getMessage()
        );

        verifyNoInteractions(
                cuentaRepository,
                transaccionRepository
        );
    }

    /**
     * Verifica que un cliente titular no pueda transferir
     * fondos desde una cuenta perteneciente a otra persona.
     */
    @Test
    void deberiaRechazarTitularSobreCuentaOrigenAjena() {
        UUID operadorId = UUID.randomUUID();
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        Cliente operador =
                Cliente.builder()
                        .id(operadorId)
                        .build();

        Cliente titularAjeno =
                Cliente.builder()
                        .id(UUID.randomUUID())
                        .build();

        CuentaCorriente origen =
                crearCuentaCorriente(
                        origenId,
                        "1000.00",
                        "0.00"
                );

        origen.getTitulares()
                .add(titularAjeno);

        CuentaCorriente destino =
                crearCuentaCorriente(
                        destinoId,
                        "500.00",
                        "0.00"
                );

        when(clienteRepository.findById(operadorId))
                .thenReturn(
                        Optional.of(operador)
                );

        configurarCuentas(
                origenId,
                origen,
                destinoId,
                destino
        );

        IllegalStateException excepcion =
                assertThrows(
                        IllegalStateException.class,
                        () -> transferenciaService.transferir(
                                request(
                                        operadorId,
                                        origenId,
                                        destinoId,
                                        new BigDecimal("100.00")
                                )
                        )
                );

        assertEquals(
                "El operador no está autorizado para transferir desde esta cuenta",
                excepcion.getMessage()
        );

        verify(
                cuentaRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transaccionRepository
        );
    }

    /**
     * Construye una solicitud de transferencia.
     */
    private TransferenciaRequestDto request(
            UUID operadorId,
            UUID origenId,
            UUID destinoId,
            BigDecimal monto) {

        return new TransferenciaRequestDto(
                operadorId,
                origenId,
                destinoId,
                monto
        );
    }

    /**
     * Configura las cuentas que devolverá el repositorio simulado.
     */
    private void configurarCuentas(
            UUID origenId,
            CuentaFinanciera origen,
            UUID destinoId,
            CuentaFinanciera destino) {

        when(cuentaRepository.findById(origenId))
                .thenReturn(
                        Optional.of(origen)
                );

        when(cuentaRepository.findById(destinoId))
                .thenReturn(
                        Optional.of(destino)
                );
    }

    /**
     * Crea un cliente titular, lo asocia a la cuenta origen
     * y configura su recuperación desde el repositorio.
     */
    private Cliente configurarTitularOperador(
            UUID operadorId,
            CuentaFinanciera origen) {

        Cliente operador =
                Cliente.builder()
                        .id(operadorId)
                        .build();

        origen.getTitulares()
                .add(operador);

        when(clienteRepository.findById(operadorId))
                .thenReturn(
                        Optional.of(operador)
                );

        return operador;
    }

    /**
     * Construye una cuenta corriente activa para las pruebas.
     */
    private CuentaCorriente crearCuentaCorriente(
            UUID id,
            String saldo,
            String descubierto) {

        CuentaCorriente cuenta =
                new CuentaCorriente();

        cuenta.setId(id);

        cuenta.setEstado(
                EstadoCuenta.ACTIVA
        );

        cuenta.setSaldoOperativo(
                new BigDecimal(saldo)
        );

        cuenta.setDescubiertoAutorizado(
                new BigDecimal(descubierto)
        );

        return cuenta;
    }
}