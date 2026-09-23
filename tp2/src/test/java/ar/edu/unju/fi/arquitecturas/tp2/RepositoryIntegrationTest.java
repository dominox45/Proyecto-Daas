package ar.edu.unju.fi.arquitecturas.tp2;

import ar.edu.unju.fi.arquitecturas.tp2.model.CajaDeAhorro;
import ar.edu.unju.fi.arquitecturas.tp2.model.Cliente;
import ar.edu.unju.fi.arquitecturas.tp2.model.CuentaCorriente;
import ar.edu.unju.fi.arquitecturas.tp2.model.Transaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoCuenta;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.EstadoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.model.enums.TipoTransaccion;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CajaDeAhorroRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.ClienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaCorrienteRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.CuentaFinancieraRepository;
import ar.edu.unju.fi.arquitecturas.tp2.repository.TransaccionRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RepositoryIntegrationTest {

    @Autowired
    private CajaDeAhorroRepository cajaDeAhorroRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CuentaCorrienteRepository cuentaCorrienteRepository;

    @Autowired
    private CuentaFinancieraRepository cuentaFinancieraRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;


    @Test
    void deberiaPersistirYConsultarCliente() {

        Cliente cliente = Cliente.builder()
                .nombre("Juan Perez")
                .cuil("20304050607")
                .email("juan.perez@test.com")
                .telefono("3884000000")
                .direccion("San Salvador de Jujuy")
                .build();

        Cliente guardado = clienteRepository.save(cliente);

        assertNotNull(guardado.getId());

        Optional<Cliente> encontradoPorCuil =
                clienteRepository.findByCuil("20304050607");

        Optional<Cliente> encontradoPorEmail =
                clienteRepository.findByEmail("juan.perez@test.com");

        assertTrue(encontradoPorCuil.isPresent());
        assertTrue(encontradoPorEmail.isPresent());

        assertEquals("Juan Perez", encontradoPorCuil.get().getNombre());
        assertEquals("juan.perez@test.com", encontradoPorEmail.get().getEmail());
    }


    @Test
    void deberiaPersistirYConsultarCajaDeAhorro() {

        CajaDeAhorro caja = new CajaDeAhorro();

        caja.setCbu("0000000000000000000001");
        caja.setAlias("CAJA.TEST.001");
        caja.setSaldoOperativo(new BigDecimal("100000.00"));
        caja.setEstado(EstadoCuenta.ACTIVA);

        caja.setTasaInteresAnual(new BigDecimal("0.250000"));
        caja.setLimiteExtraccionesMensualesSinCosto(10);

        CajaDeAhorro guardada = cajaDeAhorroRepository.save(caja);

        assertNotNull(guardada.getId());

        long cajasConLimiteSuperior =
                cajaDeAhorroRepository
                        .countByLimiteExtraccionesMensualesSinCostoGreaterThan(5);

        long cajasConTasaSuperior =
                cajaDeAhorroRepository
                        .countByTasaInteresAnualGreaterThan(
                                new BigDecimal("0.200000")
                        );

        assertEquals(1, cajasConLimiteSuperior);
        assertEquals(1, cajasConTasaSuperior);
    }


    @Test
    void deberiaPersistirYConsultarCuentaCorriente() {

        CuentaCorriente cuenta = new CuentaCorriente();

        cuenta.setCbu("0000000000000000000002");
        cuenta.setAlias("CC.TEST.001");
        cuenta.setSaldoOperativo(new BigDecimal("50000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);

        cuenta.setDescubiertoAutorizado(new BigDecimal("100000.00"));
        cuenta.setCostoComisionMantenimientoMensual(new BigDecimal("5000.00"));

        CuentaCorriente guardada =
                cuentaCorrienteRepository.save(cuenta);

        assertNotNull(guardada.getId());

        long cuentasConDescubierto =
                cuentaCorrienteRepository
                        .countByDescubiertoAutorizadoGreaterThan(
                                new BigDecimal("50000.00")
                        );

        long cuentasConComision =
                cuentaCorrienteRepository
                        .countByCostoComisionMantenimientoMensualGreaterThan(
                                new BigDecimal("3000.00")
                        );

        assertEquals(1, cuentasConDescubierto);
        assertEquals(1, cuentasConComision);
    }


    @Test
    void deberiaConsultarCuentaFinancieraPorCbuYAlias() {

        CuentaCorriente cuenta = new CuentaCorriente();

        cuenta.setCbu("0000000000000000000003");
        cuenta.setAlias("CF.TEST.001");
        cuenta.setSaldoOperativo(new BigDecimal("75000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);

        cuenta.setDescubiertoAutorizado(new BigDecimal("50000.00"));
        cuenta.setCostoComisionMantenimientoMensual(new BigDecimal("2500.00"));

        cuentaCorrienteRepository.save(cuenta);

        assertTrue(
                cuentaFinancieraRepository
                        .findByCbu("0000000000000000000003")
                        .isPresent()
        );

        assertTrue(
                cuentaFinancieraRepository
                        .findByAlias("CF.TEST.001")
                        .isPresent()
        );

        assertEquals(
                "CF.TEST.001",
                cuentaFinancieraRepository
                        .findByCbu("0000000000000000000003")
                        .get()
                        .getAlias()
        );
    }


    @Test
    void deberiaPersistirYConsultarTransaccion() {

        CuentaCorriente cuenta = new CuentaCorriente();

        cuenta.setCbu("0000000000000000000004");
        cuenta.setAlias("TRANS.TEST.001");
        cuenta.setSaldoOperativo(new BigDecimal("100000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);

        cuenta.setDescubiertoAutorizado(new BigDecimal("50000.00"));
        cuenta.setCostoComisionMantenimientoMensual(new BigDecimal("2500.00"));

        CuentaCorriente cuentaGuardada =
                cuentaCorrienteRepository.save(cuenta);

        Transaccion transaccion = Transaccion.builder()
                .fechaHora(java.time.LocalDateTime.now())
                .monto(new BigDecimal("15000.00"))
                .tipo(TipoTransaccion.DEPOSITO)
                .estadoTransaccion(EstadoTransaccion.COMPLETADA)
                .cuenta(cuentaGuardada)
                .build();

        Transaccion guardada =
                transaccionRepository.save(transaccion);

        assertNotNull(guardada.getId());

        long completadas =
                transaccionRepository
                        .countByEstadoTransaccion(
                                EstadoTransaccion.COMPLETADA
                        );

        long depositos =
                transaccionRepository
                        .countByTipo(
                                TipoTransaccion.DEPOSITO
                        );

        assertEquals(1, completadas);
        assertEquals(1, depositos);
    }
}