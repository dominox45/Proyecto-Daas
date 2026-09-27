package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class CuentaFinancieraServiceTest {

    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private CuentaFinancieraService cuentaService;

    @Test
    void deberiaDepositarYRegistrarTransaccion() {
        UUID cuentaId = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("1500.00");

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("10000.00"));

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        var resultado = cuentaService.depositar(cuentaId, monto);

        assertEquals(new BigDecimal("11500.00"), resultado.getSaldoOperativo());
        verify(cuentaRepository).save(cuenta);

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(Transaccion.class);

        verify(transaccionRepository).save(captor.capture());

        Transaccion transaccion = captor.getValue();
        assertEquals(monto, transaccion.getMonto());
        assertEquals(TipoTransaccion.DEPOSITO, transaccion.getTipo());
        assertEquals(EstadoTransaccion.COMPLETADA,
                transaccion.getEstadoTransaccion());
        assertSame(cuenta, transaccion.getCuenta());
        assertNotNull(transaccion.getFechaHora());
    }

    @Test
    void deberiaRechazarMontoNegativo() {
        UUID cuentaId = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("-100.00");

        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositar(cuentaId, monto));

        verifyNoInteractions(cuentaRepository, transaccionRepository);
    }

    @Test
    void deberiaRechazarCuentaInexistente() {
        UUID cuentaId = UUID.randomUUID();

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> cuentaService.depositar(cuentaId, new BigDecimal("100.00")));

        verify(cuentaRepository).findById(cuentaId);
        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    @Test
    void deberiaRechazarCuentaInactiva() {
        UUID cuentaId = UUID.randomUUID();
        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        assertThrows(IllegalStateException.class,
                () -> cuentaService.depositar(cuentaId, new BigDecimal("100.00")));

        verify(cuentaRepository).findById(cuentaId);
        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    @Test
    void deberiaExtraerYRegistrarTransaccion() {
        UUID cuentaId = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("300.00");

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setId(cuentaId);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("1000.00"));

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(cuenta))
                .thenReturn(cuenta);

        var resultado = cuentaService.extraer(cuentaId, monto);

        assertEquals(new BigDecimal("700.00"), resultado.getSaldoOperativo());

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(Transaccion.class);

        verify(cuentaRepository).save(cuenta);
        verify(transaccionRepository).save(captor.capture());

        Transaccion transaccion = captor.getValue();
        assertEquals(monto, transaccion.getMonto());
        assertEquals(TipoTransaccion.EXTRACCION, transaccion.getTipo());
        assertEquals(EstadoTransaccion.COMPLETADA,
                transaccion.getEstadoTransaccion());
        assertSame(cuenta, transaccion.getCuenta());
        assertNotNull(transaccion.getFechaHora());
    }

    @Test
    void deberiaRechazarExtraccionSinSaldoSuficiente() {
        UUID cuentaId = UUID.randomUUID();

        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldoOperativo(new BigDecimal("100.00"));

        when(cuentaRepository.findById(cuentaId))
                .thenReturn(Optional.of(cuenta));

        assertThrows(IllegalStateException.class,
                () -> cuentaService.extraer(cuentaId, new BigDecimal("150.00")));

        assertEquals(new BigDecimal("100.00"), cuenta.getSaldoOperativo());
        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }
}