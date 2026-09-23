
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private TransferenciaService transferenciaService;

    @Test
    void deberiaTransferirYRegistrarAmbosMovimientos() {
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("300.00");

        CuentaCorriente origen = new CuentaCorriente();
        origen.setId(origenId);
        origen.setEstado(EstadoCuenta.ACTIVA);
        origen.setSaldoOperativo(new BigDecimal("1000.00"));

        CuentaCorriente destino = new CuentaCorriente();
        destino.setId(destinoId);
        destino.setEstado(EstadoCuenta.ACTIVA);
        destino.setSaldoOperativo(new BigDecimal("500.00"));

        when(cuentaRepository.findById(origenId))
                .thenReturn(Optional.of(origen));
        when(cuentaRepository.findById(destinoId))
                .thenReturn(Optional.of(destino));

        transferenciaService.transferir(origenId, destinoId, monto);

        assertEquals(new BigDecimal("700.00"), origen.getSaldoOperativo());
        assertEquals(new BigDecimal("800.00"), destino.getSaldoOperativo());

        verify(cuentaRepository).save(origen);
        verify(cuentaRepository).save(destino);

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(Transaccion.class);

        verify(transaccionRepository, times(2)).save(captor.capture());

        List<Transaccion> movimientos = captor.getAllValues();

        Transaccion enviada = movimientos.get(0);
        assertEquals(TipoTransaccion.TRANSFERENCIA_ENVIADA,
                enviada.getTipo());
        assertEquals(monto, enviada.getMonto());
        assertEquals(EstadoTransaccion.COMPLETADA,
                enviada.getEstadoTransaccion());
        assertSame(origen, enviada.getCuenta());
        assertNotNull(enviada.getFechaHora());

        Transaccion recibida = movimientos.get(1);
        assertEquals(TipoTransaccion.TRANSFERENCIA_RECIBIDA,
                recibida.getTipo());
        assertEquals(monto, recibida.getMonto());
        assertEquals(EstadoTransaccion.COMPLETADA,
                recibida.getEstadoTransaccion());
        assertSame(destino, recibida.getCuenta());
        assertNotNull(recibida.getFechaHora());
    }

    @Test
    void deberiaRechazarTransferenciaSinSaldoSuficiente() {
        UUID origenId = UUID.randomUUID();
        UUID destinoId = UUID.randomUUID();

        CuentaCorriente origen = new CuentaCorriente();
        origen.setEstado(EstadoCuenta.ACTIVA);
        origen.setSaldoOperativo(new BigDecimal("100.00"));

        CuentaCorriente destino = new CuentaCorriente();
        destino.setEstado(EstadoCuenta.ACTIVA);
        destino.setSaldoOperativo(new BigDecimal("500.00"));

        when(cuentaRepository.findById(origenId))
                .thenReturn(Optional.of(origen));
        when(cuentaRepository.findById(destinoId))
                .thenReturn(Optional.of(destino));

        assertThrows(IllegalStateException.class,
                () -> transferenciaService.transferir(
                        origenId, destinoId, new BigDecimal("150.00")));

        assertEquals(new BigDecimal("100.00"), origen.getSaldoOperativo());
        assertEquals(new BigDecimal("500.00"), destino.getSaldoOperativo());

        verify(cuentaRepository, never()).save(any());
        verifyNoInteractions(transaccionRepository);
    }

    @Test
    void deberiaRechazarTransferenciaALaMismaCuenta() {
        UUID cuentaId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> transferenciaService.transferir(
                        cuentaId, cuentaId, new BigDecimal("100.00")));

        verifyNoInteractions(cuentaRepository, transaccionRepository);
    }
}