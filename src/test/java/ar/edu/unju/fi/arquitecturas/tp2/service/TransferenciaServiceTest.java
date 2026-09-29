package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.dto.TransferenciaRequestDto;
import ar.edu.unju.fi.arquitecturas.tp2.exception.RecursoNoEncontradoException;
import ar.edu.unju.fi.arquitecturas.tp2.exception.SaldoInsuficienteException;
import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaFinanciera;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de pruebas unitarias para TransferenciaServiceImpl.
 * Verifica el correcto funcionamiento de las reglas de negocio, validaciones
 * y persistencia en las operaciones de transferencia.
 */
@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private TransferenciaServiceImpl transferenciaService;

    /**
     * Verifica que una transferencia exitosa descuente el saldo de la cuenta origen,
     * acredite en la destino, registre ambas transacciones y devuelva el DTO correcto.
     */
    @Test
    void deberiaTransferirYRegistrarAmbosMovimientos() {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("300.00");

        CuentaCorriente origen = crearCuentaCorriente(
                origenId,
                "1000.00",
                "500.00");

        CuentaCorriente destino = crearCuentaCorriente(
                destinoId,
                "500.00",
                "500.00");

        configurarCuentas(origenId, origen, destinoId, destino);

        var response = transferenciaService.transferir(
                request(origenId, destinoId, monto));

        assertEquals(new BigDecimal("700.00"),
                origen.getSaldoOperativo());

        assertEquals(new BigDecimal("800.00"),
                destino.getSaldoOperativo());

        assertEquals(origenId, response.getCuentaOrigenId());
        assertEquals(destinoId, response.getCuentaDestinoId());
        assertEquals(monto, response.getMonto());
        assertEquals(
                EstadoTransaccion.COMPLETADA,
                response.getEstado());
        assertNotNull(response.getFechaHora());

        verify(cuentaRepository).save(origen);
        verify(cuentaRepository).save(destino);

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(Transaccion.class);

        verify(transaccionRepository, times(2))
                .save(captor.capture());

        List<Transaccion> transacciones =
                captor.getAllValues();

        Transaccion enviada = transacciones.get(0);
        Transaccion recibida = transacciones.get(1);

        assertEquals(
                TipoTransaccion.TRANSFERENCIA_ENVIADA,
                enviada.getTipo());

        assertEquals(
                TipoTransaccion.TRANSFERENCIA_RECIBIDA,
                recibida.getTipo());

        assertEquals(monto, enviada.getMonto());
        assertEquals(monto, recibida.getMonto());

        assertSame(origen, enviada.getCuenta());
        assertSame(destino, recibida.getCuenta());

        assertEquals(
                EstadoTransaccion.COMPLETADA,
                enviada.getEstadoTransaccion());

        assertEquals(
                EstadoTransaccion.COMPLETADA,
                recibida.getEstadoTransaccion());

        assertEquals(
                enviada.getFechaHora(),
                recibida.getFechaHora());

        assertEquals(
                response.getFechaHora(),
                enviada.getFechaHora());
    }

    /**
     * Verifica que se permita realizar una transferencia utilizando el descubierto
     * autorizado en una Cuenta Corriente, dejando el saldo operativo en negativo.
     */
    @Test
    void deberiaPermitirDescubiertoEnCuentaCorriente() {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CuentaCorriente origen = crearCuentaCorriente(
                origenId,
                "1000.00",
                "2000.00");

        CuentaCorriente destino = crearCuentaCorriente(
                destinoId,
                "500.00",
                "0.00");

        configurarCuentas(origenId, origen, destinoId, destino);

        transferenciaService.transferir(
                request(
                        origenId,
                        destinoId,
                        new BigDecimal("2500.00")));

        assertEquals(
                new BigDecimal("-1500.00"),
                origen.getSaldoOperativo());

        assertEquals(
                new BigDecimal("3000.00"),
                destino.getSaldoOperativo());
    }

    /**
     * Verifica que se rechace una transferencia y se lance SaldoInsuficienteException
     * si el monto supera la suma del saldo operativo y el descubierto autorizado.
     */
    @Test
    void deberiaRechazarCuandoSeExcedeElDescubierto() {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CuentaCorriente origen = crearCuentaCorriente(
                origenId,
                "1000.00",
                "2000.00");

        CuentaCorriente destino = crearCuentaCorriente(
                destinoId,
                "500.00",
                "0.00");

        configurarCuentas(origenId, origen, destinoId, destino);

        assertThrows(
                SaldoInsuficienteException.class,
                () -> transferenciaService.transferir(
                        request(
                                origenId,
                                destinoId,
                                new BigDecimal("3000.01"))));

        assertEquals(
                new BigDecimal("1000.00"),
                origen.getSaldoOperativo());

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    /**
     * Verifica que una Caja de Ahorro sea rechazada al intentar transferir un monto
     * superior a su saldo disponible, ya que no posee descubierto autorizado.
     */
    @Test
    void deberiaRechazarCajaDeAhorroSinSaldo() {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CajaDeAhorro origen = new CajaDeAhorro();
        origen.setId(origenId);
        origen.setEstado(EstadoCuenta.ACTIVA);
        origen.setSaldoOperativo(
                new BigDecimal("100.00"));

        CuentaCorriente destino = crearCuentaCorriente(
                destinoId,
                "500.00",
                "0.00");

        configurarCuentas(origenId, origen, destinoId, destino);

        assertThrows(
                SaldoInsuficienteException.class,
                () -> transferenciaService.transferir(
                        request(
                                origenId,
                                destinoId,
                                new BigDecimal("150.00"))));

        assertEquals(
                new BigDecimal("100.00"),
                origen.getSaldoOperativo());

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    /**
     * Verifica que se lance RecursoNoEncontradoException al intentar
     * transferir desde una cuenta que no existe en el repositorio.
     */
    @Test
    void deberiaRechazarCuentaOrigenInexistente() {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        when(cuentaRepository.findById(origenId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> transferenciaService.transferir(
                        request(
                                origenId,
                                destinoId,
                                new BigDecimal("100.00"))));

        verify(cuentaRepository).findById(origenId);
        verify(cuentaRepository, never())
                .findById(destinoId);
        verifyNoInteractions(transaccionRepository);
    }

    /**
     * Verifica que se lance RecursoNoEncontradoException al intentar
     * transferir hacia una cuenta que no existe en el repositorio.
     */
    @Test
    void deberiaRechazarCuentaDestinoInexistente() {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CuentaCorriente origen = crearCuentaCorriente(
                origenId,
                "1000.00",
                "500.00");

        when(cuentaRepository.findById(origenId))
                .thenReturn(Optional.of(origen));

        when(cuentaRepository.findById(destinoId))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> transferenciaService.transferir(
                        request(
                                origenId,
                                destinoId,
                                new BigDecimal("100.00"))));

        verify(cuentaRepository).findById(origenId);
        verify(cuentaRepository).findById(destinoId);

        verify(cuentaRepository, never())
                .save(any());

        verifyNoInteractions(transaccionRepository);
    }

    /**
     * Verifica que se rechace la operación si las cuentas de origen y destino son idénticas.
     */
    @Test
    void deberiaRechazarCuentasIguales() {

        UUID cuentaId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> transferenciaService.transferir(
                        request(
                                cuentaId,
                                cuentaId,
                                new BigDecimal("100.00"))));

        verifyNoInteractions(
                cuentaRepository,
                transaccionRepository);
    }

    /**
     * Verifica que se rechace la transferencia si el monto es cero o negativo.
     */
    @Test
    void deberiaRechazarMontoNoPositivo() {

        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> transferenciaService.transferir(
                        request(
                                origenId,
                                destinoId,
                                BigDecimal.ZERO)));

        verifyNoInteractions(
                cuentaRepository,
                transaccionRepository);
    }

    /**
     * Metodo auxiliar para instanciar un DTO de solicitud de transferencia.
     *
     * @param origenId  ID de la cuenta de origen.
     * @param destinoId ID de la cuenta de destino.
     * @param monto     Monto a transferir.
     * @return TransferenciaRequestDto instanciado.
     */
    private TransferenciaRequestDto request(
            UUID origenId,
            UUID destinoId,
            BigDecimal monto) {

        return new TransferenciaRequestDto(
                origenId,
                destinoId,
                monto);
    }

    /**
     * Metodo auxiliar para simular la búsqueda de cuentas en el repositorio mock.
     *
     * @param origenId  ID de la cuenta de origen.
     * @param origen    Entidad CuentaFinanciera simulada como origen.
     * @param destinoId ID de la cuenta de destino.
     * @param destino   Entidad CuentaFinanciera simulada como destino.
     */
    private void configurarCuentas(
            UUID origenId,
            CuentaFinanciera origen,
            UUID destinoId,
            CuentaFinanciera destino) {

        when(cuentaRepository.findById(origenId))
                .thenReturn(Optional.of(origen));

        when(cuentaRepository.findById(destinoId))
                .thenReturn(Optional.of(destino));
    }

    /**
     * Metodo auxiliar para instanciar rápidamente una entidad CuentaCorriente en estado activa.
     *
     * @param id          Identificador único de la cuenta.
     * @param saldo       Saldo operativo inicial.
     * @param descubierto Monto del descubierto autorizado.
     * @return CuentaCorriente instanciada.
     */
    private CuentaCorriente crearCuentaCorriente(
            UUID id,
            String saldo,
            String descubierto) {

        CuentaCorriente cuenta = new CuentaCorriente();

        cuenta.setId(id);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(
                new BigDecimal(saldo));
        cuenta.setDescubiertoAutorizado(
                new BigDecimal(descubierto));

        return cuenta;
    }
}