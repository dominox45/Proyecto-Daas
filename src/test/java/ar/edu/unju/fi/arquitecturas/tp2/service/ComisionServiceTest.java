package ar.edu.unju.fi.arquitecturas.tp2.service;

import ar.edu.unju.fi.arquitecturas.tp2.config.ComisionesProperties;
import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;
import ar.edu.unju.fi.arquitecturas.tp2.service.impl.ComisionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la liquidación mensual de comisiones.
 */
@ExtendWith(MockitoExtension.class)
class ComisionServiceTest {

    @Mock
    private CuentaFinancieraRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    private ComisionesProperties propiedades;

    private ComisionServiceImpl comisionService;

    @BeforeEach
    void configurar() {
        propiedades = new ComisionesProperties();
        propiedades.setCuentaCorriente(new BigDecimal("5000.00"));
        propiedades.setCajaAhorro(new BigDecimal("2000.00"));

        comisionService = new ComisionServiceImpl(
                cuentaRepository,
                transaccionRepository,
                propiedades
        );
    }

    /**
     * Comprueba el débito configurado para una Cuenta Corriente.
     */
    @Test
    void deberiaLiquidarComisionDeCuentaCorriente() {
        CuentaCorriente cuenta = new CuentaCorriente();
        cuenta.setSaldoOperativo(new BigDecimal("10000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);

        when(cuentaRepository.findAllByEstado(EstadoCuenta.ACTIVA))
                .thenReturn(List.of(cuenta));

        comisionService.liquidarComisionesMensuales();

        assertEquals(
                new BigDecimal("5000.00"),
                cuenta.getSaldoOperativo()
        );

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(Transaccion.class);

        verify(transaccionRepository).save(captor.capture());

        Transaccion transaccion = captor.getValue();

        assertEquals(
                new BigDecimal("5000.00"),
                transaccion.getMonto()
        );
        assertEquals(
                TipoTransaccion.DEBITO_COMISION,
                transaccion.getTipo()
        );
        assertEquals(
                EstadoTransaccion.COMPLETADA,
                transaccion.getEstadoTransaccion()
        );
        assertEquals(cuenta, transaccion.getCuenta());
        assertNull(transaccion.getOperador());
    }

    /**
     * Comprueba el débito configurado para una Caja de Ahorro.
     */
    @Test
    void deberiaLiquidarComisionDeCajaDeAhorro() {
        CajaDeAhorro cuenta = new CajaDeAhorro();
        cuenta.setSaldoOperativo(new BigDecimal("5000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);

        when(cuentaRepository.findAllByEstado(EstadoCuenta.ACTIVA))
                .thenReturn(List.of(cuenta));

        comisionService.liquidarComisionesMensuales();

        assertEquals(
                new BigDecimal("3000.00"),
                cuenta.getSaldoOperativo()
        );

        ArgumentCaptor<Transaccion> captor =
                ArgumentCaptor.forClass(Transaccion.class);

        verify(transaccionRepository).save(captor.capture());

        assertEquals(
                new BigDecimal("2000.00"),
                captor.getValue().getMonto()
        );
    }

    /**
     * Verifica que el proceso solicite únicamente cuentas activas.
     */
    @Test
    void deberiaConsultarSolamenteCuentasActivas() {
        when(cuentaRepository.findAllByEstado(EstadoCuenta.ACTIVA))
                .thenReturn(List.of());

        comisionService.liquidarComisionesMensuales();

        verify(cuentaRepository)
                .findAllByEstado(EstadoCuenta.ACTIVA);

        verify(cuentaRepository, never())
                .save(org.mockito.ArgumentMatchers.any());

        verify(transaccionRepository, never())
                .save(org.mockito.ArgumentMatchers.any());
    }
}
